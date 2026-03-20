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
            "Rent",
            "Education",
            "Travel",
            "Other"
        ).joinToString(", ")

        return """
            You are a finance data extraction engine.
            Extract exactly ONE transaction (income or expense) from the user's text.

            Return ONLY a single JSON object (no markdown, no extra keys) with these fields:
            - "amount": number (e.g., 150 or 150.50). Do not include currency symbols in this field.
            - "currency": string (e.g., "TL", "USD", "EUR").
            - "category": string. Choose one from: $categories
            - "type": string. Choose either "INCOME" or "EXPENSE".

            User text: "$userText"
        """.trimIndent()
    }

    private fun parseJsonFromModel(raw: String): ParsedTransaction? {
        val trimmed = raw.trim()

        // Model output might be wrapped in ```json ... ```. We only need the first {...} block.
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) return null

        val jsonText = trimmed.substring(start, end + 1)
        val obj = JSONObject(jsonText)

        val amountValue = obj.opt("amount")
        val amount = when (amountValue) {
            is Number -> amountValue.toDouble()
            is String -> amountValue.replace(',', '.').toDoubleOrNull()
            else -> null
        } ?: return null

        val currency = obj.optString("currency", "").trim()
        val category = obj.optString("category", "").trim()

        val typeRaw = obj.optString("type", "").trim().uppercase()
        val type = when (typeRaw) {
            "INCOME" -> TransactionType.INCOME
            "EXPENSE" -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }

        if (currency.isBlank() || category.isBlank()) return null

        return ParsedTransaction(
            amount = amount,
            currency = currency,
            category = category,
            type = type
        )
    }
}

