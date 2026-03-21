package com.example.spendwiseai.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.spendwiseai.ai.gemini.GeminiInsightsGenerator
import com.example.spendwiseai.data.repository.InsightsRepository
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.data.db.InsightEntity
import com.example.spendwiseai.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class InsightsViewModel(
    private val insightsRepository: InsightsRepository,
    private val transactionRepository: TransactionRepository,
    private val generator: GeminiInsightsGenerator
) : ViewModel() {

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    val uiState = combine(
        insightsRepository.observeAll(),
        _isGenerating
    ) { insights, generating ->
        InsightsUiState(
            isLoading = generating || insights.isEmpty(),
            isGenerating = generating,
            insights = insights
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    init {
        refreshInsight()
    }

    fun refreshInsight() {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val now = Instant.now()
                val zone = ZoneId.systemDefault()
                val nowZoned = now.atZone(zone)

                val startOfToday = nowZoned.toLocalDate()
                    .atStartOfDay(zone).toInstant().toEpochMilli()
                val endOfToday = startOfToday + 24L * 60L * 60L * 1000L
                val startOfRange = nowZoned.minusDays(6)
                    .toLocalDate().atStartOfDay(zone)
                    .toInstant().toEpochMilli()

                val totalIncome = transactionRepository
                    .getTotalAmountForType(TransactionType.INCOME)
                val totalExpense = transactionRepository
                    .getTotalAmountForType(TransactionType.EXPENSE)
                val totalBalance = totalIncome - totalExpense
                val dailySpending = transactionRepository
                    .getAmountBetween(TransactionType.EXPENSE, startOfToday, endOfToday)
                val categoryTotals = transactionRepository
                    .getCategoryTotalsBetween(TransactionType.EXPENSE, startOfRange, endOfToday)

                val content = generator.generateInsights(
                    totalBalance = totalBalance,
                    dailySpending = dailySpending,
                    categoryTotals = categoryTotals
                )

                insightsRepository.upsert(
                    InsightEntity(
                        weekStartMillis = System.currentTimeMillis(),
                        content = content,
                        createdAtMillis = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.e("InsightsVM", "Rapor üretilemedi: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }
}

