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
            You are a personal finance coach.

            User summary:
            - Total balance (income - expense): $totalBalance
            - Today's spending: $dailySpending
            - Category spending totals (last 7 days): $top

            Give concise, actionable insights (2-4 short sentences).
            Include one suggestion to reduce spending and one suggestion to save.
            If entertainment is highest, mention it explicitly.

            Return ONLY plain text (no markdown).
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
        val totalExpense = categoryTotals.sumOf { it.totalAmount }
        val topShare = if (totalExpense > 0) (top?.totalAmount ?: 0.0) / totalExpense else 0.0

        return if (top != null && topShare >= 0.35) {
            "Your biggest spending share is $${top.categoryName}. Consider trimming it by about 10% next month. Set a weekly cap so you don't drift, and try saving at least 10% of any leftover balance. Today your spending is $dailySpending."
        } else {
            "You're building a balanced pattern. Keep reviewing your categories weekly, and aim to save 10% of any positive leftover balance. If you want faster progress, pick one category to reduce by 10% and stick to it for 2 weeks. Today's spending is $dailySpending."
        }.replace("$$", "$")
    }
}

