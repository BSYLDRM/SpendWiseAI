package com.example.spendwiseai.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwiseai.ai.gemini.GeminiInsightsGenerator
import com.example.spendwiseai.data.repository.InsightsRepository
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.data.db.InsightEntity
import com.example.spendwiseai.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class InsightsViewModel(
    private val insightsRepository: InsightsRepository,
    private val transactionRepository: TransactionRepository,
    private val generator: GeminiInsightsGenerator
) : ViewModel() {

    private val isGenerating = MutableStateFlow(false)

    val uiState = combine(
        insightsRepository.observeAll(),
        isGenerating
    ) { insights, generating ->
        InsightsUiState(
            isLoading = generating || insights.isEmpty(),
            insights = insights
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    init {
        ensureCurrentWeekInsight()
    }

    private fun ensureCurrentWeekInsight() {
        viewModelScope.launch {
            val now = Instant.now()
            val zone = ZoneId.systemDefault()
            val nowZoned = now.atZone(zone)

            val weekStart = nowZoned
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate()
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()

            val existing = insightsRepository.findByWeekStart(weekStart)
            if (existing != null) return@launch
            generateAndStoreInsight(weekStart)
        }
    }

    fun refreshInsights() {
        viewModelScope.launch {
            val weekStart = currentWeekStartMillis()
            generateAndStoreInsight(weekStart)
        }
    }

    private fun currentWeekStartMillis(): Long {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val nowZoned = now.atZone(zone)
        return nowZoned
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toLocalDate()
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
    }

    private suspend fun generateAndStoreInsight(weekStart: Long) {
        if (isGenerating.value) return
        isGenerating.update { true }
        try {
            val now = Instant.now()
            val zone = ZoneId.systemDefault()
            val nowZoned = now.atZone(zone)
            val startOfToday = nowZoned.toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfToday = startOfToday + 24L * 60L * 60L * 1000L
            val startOfRange = nowZoned.minusDays(6).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
            val totalIncome = transactionRepository.getTotalAmountForType(TransactionType.INCOME)
            val totalExpense = transactionRepository.getTotalAmountForType(TransactionType.EXPENSE)
            val totalBalance = totalIncome - totalExpense

            val dailySpending = transactionRepository.getAmountBetween(TransactionType.EXPENSE, startOfToday, endOfToday)
            val categoryTotals = transactionRepository.getCategoryTotalsBetween(
                type = TransactionType.EXPENSE,
                startMillis = startOfRange,
                endMillis = endOfToday
            )

            val content = generator.generateInsights(
                totalBalance = totalBalance,
                dailySpending = dailySpending,
                categoryTotals = categoryTotals
            )

            insightsRepository.upsert(
                InsightEntity(
                    weekStartMillis = weekStart,
                    content = content,
                    createdAtMillis = System.currentTimeMillis()
                )
            )
        } finally {
            isGenerating.update { false }
        }
    }
}

