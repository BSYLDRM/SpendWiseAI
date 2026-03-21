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
        if (apiKey.isBlank()) return fallback.parseExpense(text)

        val prompt = buildPrompt(text)
        val client = GeminiRestClient(apiKey = apiKey)

        return try {
            val raw = client.generateContentText(prompt)
            parseJsonFromModel(raw)?.takeIf {
                it.amount > 0 && it.currency.isNotBlank() && it.category.isNotBlank()
            } ?: fallback.parseExpense(text)
        } catch (_: Throwable) {
            fallback.parseExpense(text)
        }
    }

    private fun buildPrompt(userText: String): String {
        return """
            Sen bir finansal analiz asistanısın. Kullanıcının yazdığı metni analiz et.
            SADECE aşağıdaki JSON'ı döndür, başka hiçbir şey yazma:
            {"amount": 250.0, "currency": "TL", "category": "Groceries", "type": "EXPENSE"}

            GELİR mi GİDER mi:
            - maaş, ikramiye, prim, kazandım, geldi, aldım (para), iade, freelance, yemek parası → INCOME
            - Geri kalan her şey → EXPENSE

            GİDER KATEGORİLERİ:
            - Groceries → market, migros, bim, a101, şok, ekmek, süt, yumurta, sebze, meyve, manav
            - Food & Drink → kahve, çay, restoran, cafe, yemek, döner, burger, pizza, starbucks
            - Transportation → benzin, akaryakıt, otobüs, metro, taksi, uber, dolmuş, uçak
            - Technology → bilgisayar, telefon, tablet, kulaklık, elektronik, yazılım, oyun, steam
            - Shopping → kıyafet, ayakkabı, çanta, zara, lcw, mağaza, avm
            - Bills & Utilities → elektrik, su, doğalgaz, internet, fatura, aidat, kira
            - Health → eczane, ilaç, doktor, hastane, muayene, diş
            - Education → kitap, kurs, eğitim, okul, ders
            - Entertainment → sinema, konser, netflix, spotify, oyun, bilet

            GELİR KATEGORİLERİ:
            - Salary → maaş, ikramiye, prim, aylık
            - Freelance → freelance, proje ücreti, danışmanlık, yazılım geliri
            - Refund → iade, geri ödeme, cashback, iptal iadesi
            - Meal Allowance → yemek parası, yemek kartı, yemek yardımı
            - Investment → faiz, temettü, kira geliri, yatırım getirisi
            - Gift → hediye, bağış, harçlık
            - Other Income → diğer gelir

            Kullanıcı metni: "$userText"
        """.trimIndent()
    }

    private fun parseJsonFromModel(raw: String): ParsedTransaction? {
        return try {
            val start = raw.indexOf('{')
            val end = raw.lastIndexOf('}')
            if (start == -1 || end == -1 || end <= start) return null
            val json = JSONObject(raw.substring(start, end + 1))

            val amount = json.optDouble("amount", Double.NaN)
            val currency = json.optString("currency", "").trim()
            val category = json.optString("category", "").trim()
            val type = when (json.optString("type", "").trim().uppercase()) {
                "INCOME" -> TransactionType.INCOME
                "EXPENSE" -> TransactionType.EXPENSE
                else -> return null
            }

            if (amount.isNaN() || amount <= 0.0 || currency.isBlank() || category.isBlank()) null
            else ParsedTransaction(amount, currency, category, type)
        } catch (_: Throwable) {
            null
        }
    }
}