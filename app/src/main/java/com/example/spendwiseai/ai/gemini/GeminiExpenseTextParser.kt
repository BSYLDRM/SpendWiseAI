package com.example.spendwiseai.ai.gemini

import com.example.spendwiseai.domain.ai.ExpenseTextParser
import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType
import org.json.JSONObject

class GeminiExpenseTextParser(
    private val apiKey: String,
    private val fallback: ExpenseTextParser = RegexExpenseTextParser()
) : ExpenseTextParser {

    override suspend fun parseExpense(text: String): ParsedTransaction {
        if (apiKey.isBlank()) {
            // No API key configured yet; use a local heuristic so UX still works.
            return fallback.parseExpense(text)
        }

        val prompt = buildPrompt(text)
        val client = GeminiRestClient(apiKey = apiKey)

        return try {
            val raw = client.generateContentText(prompt)
            parseJsonFromModel(raw)?.let { parsed ->
                if (parsed.amount > 0 && parsed.currency.isNotBlank() && parsed.category.isNotBlank()) {
                    parsed
                } else {
                    fallback.parseExpense(text)
                }
            } ?: fallback.parseExpense(text)
        } catch (_: Throwable) {
            // Parsing can fail (network, quota, or model output format). Fall back gracefully.
            fallback.parseExpense(text)
        }
    }

    private fun buildPrompt(userText: String): String {
        val categories = listOf(
            "Food & Drink",
            "Groceries",
            "Transportation",
            "Entertainment",
            "Shopping",
            "Bills & Utilities",
            "Health",
            "Other"
        ).joinToString(", ")
        val hints = """
            - Groceries (market, süpermarket): Migros, BİM, A101, ŞOK, Carrefour
            - Food & Drink (restoran, cafe, kahve, fast food)
            - Transportation (benzin, akaryakıt, OPET, Shell, BP, otobüs, taksi)
            - Entertainment (sinema, konser, bar, oyun)
            - Shopping (giyim, elektronik, AVM)
            - Bills & Utilities (elektrik, su, doğalgaz, internet)
            - Health (eczane, hastane, doktor)
            - Other (hiçbirine uymuyorsa)
        """.trimIndent()

        return """
            You are a finance data extraction engine.
            Extract exactly ONE transaction (income or expense) from the user's text.

            Return ONLY a single JSON object (no markdown, no extra keys) with these fields:
            - "amount": number (e.g., 150 or 150.50). Do not include currency symbols in this field.
            - "currency": string (e.g., "TL", "USD", "EUR").
            - "category": string. Choose one from: $categories
            - "type": string. Choose either "INCOME" or "EXPENSE".
            Use these Turkish hints for category detection:
            $hints

            User text: "$userText"
        """.trimIndent()
    }

    private fun parseJsonFromModel(raw: String): ParsedTransaction? {
        return try {
            val jsonText = extractJsonObject(raw) ?: return null
            val json = JSONObject(jsonText)

            val amount = json.optDouble("amount", Double.NaN)
            val currency = json.optString("currency", "").trim()
            val category = json.optString("category", "").trim()
            val type = when (json.optString("type", "").trim().uppercase()) {
                "INCOME" -> TransactionType.INCOME
                "EXPENSE" -> TransactionType.EXPENSE
                else -> return null
            }

            if (amount.isNaN() || amount <= 0.0 || currency.isBlank() || category.isBlank()) {
                return null
            }

            ParsedTransaction(
                amount = amount,
                currency = currency,
                category = category,
                type = type
            )
        } catch (_: Throwable) {
            null
        }
    }

    private fun extractJsonObject(raw: String): String? {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) return null
        return raw.substring(start, end + 1).trim()
    }
}

