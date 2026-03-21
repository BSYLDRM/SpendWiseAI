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
        return """
            Sen bir finansal analiz asistanısın. Kullanıcının yazdığı metni 
            analiz et ve aşağıdaki JSON formatında döndür.

            Kullanıcı metni: "$userText"

            GELİR Mİ GİDER Mİ:
            - "geldi, aldım maaş, kazandım, ödeme aldım" → INCOME
            - Geri kalan her şey → EXPENSE

            KATEGORİLER (çok dikkatli seç):
            - Groceries → market, migros, bim, a101, ekmek, süt, yumurta, 
              sebze, meyve, manav, alışveriş
            - Food & Drink → kahve, çay, restoran, cafe, yemek, döner, 
              burger, pizza, starbucks, tavuk, et, balık, lokanta
            - Transportation → benzin, akaryakıt, otobüs, metro, taksi, 
              uber, dolmuş, uçak bileti, araç
            - Technology → bilgisayar, telefon, tablet, kulaklık, 
              elektronik, yazılım, uygulama, oyun, steam, teknoloji
            - Shopping → kıyafet, ayakkabı, çanta, zara, lcw, hm, 
              mağaza, avm, online alışveriş
            - Bills & Utilities → elektrik, su, doğalgaz, internet, 
              fatura, aidat, kira
            - Health → eczane, ilaç, doktor, hastane, muayene, diş
            - Education → kitap, kurs, eğitim, okul, ders, sınav
            - Entertainment → sinema, konser, netflix, spotify, oyun, 
              bilet, eğlence, gece
            - Salary → maaş, ikramiye, prim, ödeme aldım
            - Other → hiçbirine uymuyorsa

            SADECE bu JSON'ı döndür, başka hiçbir şey yazma:
            {
              "amount": 250.0,
              "currency": "TL", 
              "category": "Groceries",
              "type": "EXPENSE"
            }
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

