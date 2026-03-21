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

            GİDER KATEGORİLERİ (çok dikkatli seç, örneklere bak):

            - Groceries → market, migros, bim, a101, şok, carrefour, ekmek, süt, yumurta,
              sebze, meyve, manav, et, tavuk, balık, peynir, alışveriş, gıda, erzak

            - Food & Drink → kahve, çay, restoran, cafe, yemek, döner, burger, pizza,
              starbucks, mcdonalds, kfc, lokanta, bistro, bar, pub, bira, alkol, içki,
              kokteyl, şarap, viski, rakı, içecek, meşrubat, su (şişe), meyve suyu

            - Transportation → benzin, motorin, mazot, akaryakıt, yakıt, otobüs, metro,
              taksi, uber, dolmuş, uçak, tren, vapur, bilet, otogar, otopark, servis,
              opet, shell, bp, total, petrol ofisi, araç, lastik, oto yıkama

            - Technology → bilgisayar, laptop, telefon, tablet, kulaklık, klavye, mouse,
              monitör, elektronik, yazılım, uygulama, oyun, steam, apple, samsung, xbox,
              playstation, abonelik, domain, hosting

            - Shopping → kıyafet, ayakkabı, çanta, cüzdan, saat, gözlük, takı, aksesuar,
              zara, lcw, h&m, mango, koton, bershka, pull&bear, mağaza, avm, online alışveriş,
              trendyol, hepsiburada, amazon, n11

            - Bills & Utilities → elektrik, su, doğalgaz, internet, telefon faturası,
              fatura, aidat, apartman, sigorta, vergi, kredi kartı

            - Rent → kira, ev kirası, konut, daire, kiracı

            - Health → eczane, ilaç, doktor, hastane, muayene, diş, gözlük,
              check-up, tahlil, reçete, vitamin, takviye, spor salonu üyelik

            - Education → kitap, kurs, eğitim, okul, ders, sınav, üniversite,
              özel ders, dershane, seminer, sertifika, udemy, coursera

            - Entertainment → sinema, konser, tiyatro, netflix, spotify, youtube premium,
              oyun konsolu oyunu, bilet, eğlence, lunapark, müze, gezi, tatil, otel

            GELİR KATEGORİLERİ:
            - Salary → maaş, ikramiye, prim, aylık, yıllık izin ücreti
            - Freelance → freelance, proje ücreti, danışmanlık, yazılım geliri, serbest çalışma
            - Refund → iade, geri ödeme, cashback, iptal iadesi, para iadesi
            - Meal Allowance → yemek parası, yemek kartı, yemek yardımı, yemek ücreti
            - Investment → faiz, temettü, kira geliri, yatırım getirisi, hisse senedi
            - Gift → hediye, bağış, harçlık, para hediyesi
            - Other Income → diğer gelir, ek iş, part-time

            ÖNEMLI KURALLAR:
            - Bira, şarap, rakı, viski, alkol → Food & Drink (içki de yiyecek içecek kategorisi)
            - Mazot, motorin → Transportation (benzin ile aynı)
            - Kira, ev kirası → Rent (Other değil!)
            - Bar, pub → Food & Drink
            - Spor salonu → Health
            - Netflix, Spotify → Entertainment
            - Para birimi belirtilmemişse TL yaz

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