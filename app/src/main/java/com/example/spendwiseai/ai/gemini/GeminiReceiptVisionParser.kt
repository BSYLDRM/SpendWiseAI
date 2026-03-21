package com.example.spendwiseai.ai.gemini

import android.graphics.Bitmap
import android.util.Log
import com.example.spendwiseai.domain.ai.ReceiptVisionParser
import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class GeminiReceiptVisionParser(
    private val apiKey: String,
    private val fallback: ReceiptVisionParser? = null
) : ReceiptVisionParser {

    override suspend fun parseReceipt(bitmap: Bitmap): ParsedTransaction {
        val client = GeminiRestClient(apiKey = apiKey)
        val jpegBytes = bitmapToJpegBytes(bitmap)

        val prompt = """
            Analyze this receipt image carefully.
            Return ONLY a valid JSON object, no markdown, no explanation, nothing else.

            JSON format:
            {
              "amount": 125.50,
              "currency": "TL",
              "category": "Groceries",
              "type": "EXPENSE"
            }

            Category must be exactly ONE of these:
            - Groceries → if store is: Migros, BİM, A101, ŞOK, CarrefourSA, Hakmar, market, manav
            - Food & Drink → if store is: restoran, cafe, Starbucks, McDonald's, Burger King, kahveci, pastane
            - Transportation → if store is: OPET, Shell, BP, Total, petrol, akaryakıt, benzin istasyonu
            - Entertainment → if store is: sinema, konser, bilet, Steam, Netflix
            - Shopping → if store is: Zara, LC Waikiki, H&M, Trendyol, elektronik mağaza, AVM
            - Bills & Utilities → if store is: elektrik, su, doğalgaz, internet faturası
            - Health → if store is: eczane, hastane, klinik, optik
            - Other → if none of the above match

            Amount must be the TOTAL amount on the receipt (en büyük tutar, genelde en altta).
        """.trimIndent()

        return try {
            val rawResponse = client.generateContentTextWithImage(prompt, jpegBytes)
            Log.d("GeminiDebug", "Raw response: $rawResponse")

            parseJsonFromModel(rawResponse)
        } catch (e: Exception) {
            Log.e("GeminiDebug", "Hata Oluştu: ${e.message}")
            fallbackParsed("Model yaniti alinamadi: ${e.message}")
        }
    }

    private fun parseJsonFromModel(raw: String): ParsedTransaction {
        return try {
            val cleaned = cleanModelJson(raw)
            Log.d("GeminiDebug", "Cleaned JSON: $cleaned")
            val json = JSONObject(cleaned)
            var amount = json.optDouble("amount", 0.0)
            val currency = json.optString("currency", "TL").trim().ifBlank { "TL" }
            var category = json.optString("category", "Other").trim().ifBlank { "Other" }
            val type = when (json.optString("type", "").trim().uppercase()) {
                "INCOME" -> TransactionType.INCOME
                "EXPENSE" -> TransactionType.EXPENSE
                else -> TransactionType.EXPENSE
            }

            if (amount <= 0.0) {
                amount = parseAmountFromText(raw)
                Log.d("GeminiDebug", "Regex amount: $amount")
            }
            if (category.equals("Other", ignoreCase = true)) {
                category = parseCategoryFromText(raw)
            }
            Log.d("GeminiDebug", "Parsed amount: $amount")
            Log.d("GeminiDebug", "Parsed category: $category")
            ParsedTransaction(amount = amount, currency = currency, category = category, type = type)
        } catch (e: Throwable) {
            Log.e("GeminiDebug", "JSON parse basarisiz, text fallback deneniyor: ${e.message}")
            val amount = parseAmountFromText(raw)
            val category = parseCategoryFromText(raw)
            Log.d("GeminiDebug", "Parsed amount: $amount")
            Log.d("GeminiDebug", "Parsed category: $category")
            ParsedTransaction(
                amount = amount,
                currency = "TL",
                category = category,
                type = TransactionType.EXPENSE
            )
        }
    }

    private fun cleanModelJson(raw: String): String {
        val withoutFence = raw
            .replace("```json", "", ignoreCase = true)
            .replace("```", "")
            .trim()

        val lines = withoutFence.lines()
        var startLine = -1
        var endLine = -1

        for (i in lines.indices) {
            if (lines[i].contains("{")) {
                startLine = i
                break
            }
        }
        for (i in lines.indices.reversed()) {
            if (lines[i].contains("}")) {
                endLine = i
                break
            }
        }
        if (startLine != -1 && endLine != -1 && endLine >= startLine) {
            return lines.subList(startLine, endLine + 1).joinToString("\n").trim()
        }
        return withoutFence
    }

    private fun parseAmountFromText(raw: String): Double {
        val regex = Regex("""\d+[.,]\d{2}""")
        val amounts = regex.findAll(raw)
            .mapNotNull { it.value.replace(',', '.').toDoubleOrNull() }
            .toList()
        return amounts.maxOrNull() ?: 0.0
    }

    private fun parseCategoryFromText(raw: String): String {
        val text = raw.lowercase()
        return when {
            listOf("migros", "bim", "a101", "şok", "sok", "market", "manav").any { text.contains(it) } -> "Groceries"
            listOf("cafe", "kahve", "restoran", "yemek", "starbucks").any { text.contains(it) } -> "Food & Drink"
            listOf("opet", "shell", "bp", "benzin", "akaryakıt", "akaryakit", "petrol").any { text.contains(it) } -> "Transportation"
            listOf("eczane", "hastane", "ilaç", "ilac").any { text.contains(it) } -> "Health"
            listOf("elektrik", "su", "doğalgaz", "dogalgaz", "fatura").any { text.contains(it) } -> "Bills & Utilities"
            listOf("sinema", "konser", "netflix").any { text.contains(it) } -> "Entertainment"
            listOf("zara", "lcw", "mağaza", "magaza", "giyim").any { text.contains(it) } -> "Shopping"
            else -> "Other"
        }
    }

    private fun fallbackParsed(reason: String): ParsedTransaction {
        Log.e("GeminiDebug", reason)
        return ParsedTransaction(
            amount = 0.0,
            currency = "TL",
            category = "Other",
            type = TransactionType.EXPENSE
        )
    }

    private fun bitmapToJpegBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }

}