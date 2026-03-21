package com.example.spendwiseai.ai.gemini

import android.util.Log
import com.example.spendwiseai.data.db.dao.CategoryTotal
import java.util.Locale

class GeminiInsightsGenerator(
    private val apiKey: String,
) {

    suspend fun generateInsights(
        totalBalance: Double,
        dailySpending: Double,
        categoryTotals: List<CategoryTotal>
    ): String {
        if (apiKey.isBlank()) {
            return fallbackInsights(totalBalance, dailySpending, categoryTotals)
        }

        val top = categoryTotals.take(5).joinToString(", ") {
            "${it.categoryName}=${String.format(Locale.US, "%.0f", it.totalAmount)}TL"
        }

        // Prompt kısa ve net — giriş cümlesi yok, direkt rapor
        val prompt = """
            Write a Turkish personal finance report.
            NO greeting. Start directly with the first emoji section.
            Use ONLY period as decimal separator (e.g. 90667.05 NOT 90.667,05).
            Each section maximum 2 sentences. Be concise.

            VERİLER:
            Bakiye: ${String.format(Locale.US, "%.2f", totalBalance)} TL
            Bugün harcama: ${String.format(Locale.US, "%.2f", dailySpending)} TL
            Kategoriler: $top

            RAPOR FORMATI (her bölümü yaz, kısa ve öz):

            💰 GENEL DURUM
            (bakiye hakkında 2 cümle)

            📊 HARCAMA ANALİZİ
            (en yüksek kategori ve yüzdesi, 2 cümle)

            ⚠️ DİKKAT
            (azaltılması gereken 2 kalem, rakam ver)

            🎯 HEDEFLER
            1. (somut rakamla hedef)
            2. (somut rakamla hedef)
            3. (somut rakamla hedef)

            💡 ÖNERİLER
            1. (kişisel öneri)
            2. (kişisel öneri)
            3. (kişisel öneri)
        """.trimIndent()

        return try {
            val client = GeminiRestClient(apiKey = apiKey)
            val result = client.generateContentText(prompt)
            Log.d("InsightsDebug", "Gemini cevabı geldi: $result")
            result.trim()
        } catch (e: Throwable) {
            Log.e("InsightsDebug", "HATA: ${e.message}")
            fallbackInsights(totalBalance, dailySpending, categoryTotals)
        }
    }

    private fun fallbackInsights(
        totalBalance: Double,
        dailySpending: Double,
        categoryTotals: List<CategoryTotal>
    ): String {
        val top = categoryTotals.firstOrNull()
        val totalExpense = categoryTotals.sumOf { it.totalAmount }
        val topShare = if (totalExpense > 0) (top?.totalAmount ?: 0.0) / totalExpense else 0.0

        return if (top != null && topShare >= 0.35) {
            """
            💰 GENEL DURUM
            Bakiyeniz ${String.format(Locale.US, "%.2f", totalBalance)} TL. Harcamalarınız gelirinizi aşıyor.

            📊 HARCAMA ANALİZİ
            En yüksek harcama ${top.categoryName} kategorisinde: ${String.format(Locale.US, "%.2f", top.totalAmount)} TL (%${(topShare * 100).toInt()}).

            ⚠️ DİKKAT
            ${top.categoryName} harcamasını %20 azaltın. Bugün ${String.format(Locale.US, "%.2f", dailySpending)} TL harcadınız.

            🎯 HEDEFLER
            1. ${top.categoryName} için aylık limit belirleyin.
            2. Günlük harcamayı takip edin.
            3. Aylık tasarruf hedefi koyun.

            💡 ÖNERİLER
            1. Gereksiz alışverişlerden kaçının.
            2. Haftalık bütçe planlayın.
            3. Tasarruf hesabı açın.
            """.trimIndent()
        } else {
            """
            💰 GENEL DURUM
            Bakiyeniz ${String.format(Locale.US, "%.2f", totalBalance)} TL.

            📊 HARCAMA ANALİZİ
            Harcamalarınız dengeli görünüyor. Bugün ${String.format(Locale.US, "%.2f", dailySpending)} TL harcadınız.

            ⚠️ DİKKAT
            Harcama kategorilerinizi düzenli kontrol edin.

            🎯 HEDEFLER
            1. Aylık bütçe oluşturun.
            2. Tasarruf oranını artırın.
            3. Gereksiz abonelikleri iptal edin.

            💡 ÖNERİLER
            1. Haftalık harcama özeti çıkarın.
            2. En yüksek kalemi %10 azaltın.
            3. Acil durum fonu oluşturun.
            """.trimIndent()
        }
    }
}