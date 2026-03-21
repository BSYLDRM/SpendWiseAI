package com.example.spendwiseai.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId

class DashboardViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState = combine(
        transactionRepository.observeTotalAmountForType(TransactionType.INCOME),
        transactionRepository.observeTotalAmountForType(TransactionType.EXPENSE),
        transactionRepository.observeAmountBetween(
            TransactionType.EXPENSE,
            getTodayStart(),
            getTodayEnd()
        ),
        transactionRepository.observeCategoryTotalsBetween(
            TransactionType.EXPENSE,
            getRangeStart(),
            getTodayEnd()
        ),
        transactionRepository.observeCategoryTotalsBetween(
            TransactionType.INCOME,
            getRangeStart(),
            getTodayEnd()
        )
    ) { totalIncome, totalExpense, dailySpending, expenseCategoryTotals, incomeCategoryTotals ->
        DashboardUiState(
            isLoading = false,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalBalance = totalIncome - totalExpense,
            dailySpending = dailySpending,
            expenseCategoryTotals = expenseCategoryTotals,
            incomeCategoryTotals = incomeCategoryTotals
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(isLoading = true)
    )

    // Fix 13: her çağrıda anlık zaman hesaplanır, gece yarısı sorunu olmaz
    private fun getTodayStart(): Long {
        val zone = ZoneId.systemDefault()
        return Instant.now().atZone(zone).toLocalDate()
            .atStartOfDay(zone).toInstant().toEpochMilli()
    }

    private fun getTodayEnd(): Long = getTodayStart() + 24L * 60L * 60L * 1000L

    private fun getRangeStart(): Long {
        val zone = ZoneId.systemDefault()
        return Instant.now().atZone(zone).minusDays(6).toLocalDate()
            .atStartOfDay(zone).toInstant().toEpochMilli()
    }
}