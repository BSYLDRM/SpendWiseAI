package com.example.spendwiseai.ai.gemini

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

        val top = categoryTotals.take(5).joinToString(", ") { "${it.categoryName}=${String.format(Locale.US, "%.2f", it.totalAmount)}" }
        val prompt = """
            Sen bir kişisel finans uzmanısın. Aşağıdaki verileri analiz et 
            ve Türkçe detaylı bir rapor yaz.

            VERİLER:
            - Toplam bakiye: $totalBalance TL
            - Bugünkü harcama: $dailySpending TL
            - Kategori harcamaları: $top

            RAPORU ŞÖYLE YAPI, her bölüm arasında boş satır bırak:

            💰 GENEL DURUM
            Bakiyeni ve genel finansal durumunu 2 cümleyle değerlendir.

            📊 HARCAMA ANALİZİ  
            En çok harcadığın kategoriyi belirt, bu normalin üzerinde mi 
            yoksa makul mü yorumunu yap. Rakamları kullan.

            ⚠️ DİKKAT EDİLMESİ GEREKENLER
            Azaltılması gereken 2 harcama kalemi ve neden azaltılması 
            gerektiğini açıkla. Somut rakam ver.

            🎯 BU AY İÇİN HEDEFLER
            3 madde halinde somut ve ölçülebilir hedef yaz.
            Her hedef rakam içersin. Örnek: Market harcamasını 
            500 TL altında tut.

            💡 TASARRUF ÖNERİLERİ
            Bu kişinin harcama profiline özel 3 pratik tasarruf önerisi.
            Genel değil, verilere dayalı kişisel öneriler olsun.

            Toplam 15-20 cümle olsun. Samimi, motive edici bir dil kullan.
        """.trimIndent()

        return try {
            val client = GeminiRestClient(apiKey = apiKey)
            client.generateContentText(prompt).trim()
        } catch (_: Throwable) {
            fallbackInsights(totalBalance, dailySpending, categoryTotals)
        }
    }

    private fun fallbackInsights(
        totalBalance: Double,
        dailySpending: Double,
        categoryTotals: List<CategoryTotal>
    ): String {
        val top = categoryTotals.firstOrNull()
        val second = categoryTotals.getOrNull(1)?.categoryName ?: "Yok"
        val status = if (totalBalance >= 0) "denge pozitif" else "denge negatif"
        return """
            💰 GENEL DURUM
            Toplam bakiyen ${"%.2f".format(Locale.US, totalBalance)} TL ve şu an $status. Bu tabloyu düzenli takip etmen finansal kontrolü güçlendirir.

            📊 HARCAMA ANALİZİ
            En çok harcama yaptığın kategori ${top?.categoryName ?: "Other"} görünüyor. İkinci sırada $second var.

            ⚠️ DİKKAT EDİLMESİ GEREKENLER
            1) En yüksek kategori harcamasında %10 azaltım dene.
            2) Günlük harcamayı ${"%.2f".format(Locale.US, dailySpending)} TL seviyesinin altında tutmaya çalış.

            🎯 BU AY İÇİN HEDEFLER
            1. Haftalık market harcamasını 500 TL altında tut.
            2. Aylık eğlence harcamasını 1000 TL altında sınırla.
            3. Her ay en az 1500 TL birikim ayır.

            💡 TASARRUF ÖNERİLERİ
            1. En yüksek iki kategoride toplam %15 kısma hedefi koy.
            2. Abonelik ve tekrar eden küçük ödemeleri gözden geçir.
            3. Günlük harcama limitini yazılı takip et.
        """.trimIndent()
    }
}

