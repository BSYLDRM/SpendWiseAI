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
import java.time.ZonedDateTime

class DashboardViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    private val now = Instant.now()
    private val zone = ZoneId.systemDefault()
    private val nowZoned: ZonedDateTime = now.atZone(zone)
    private val startOfToday = nowZoned.toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
    private val endOfToday = startOfToday + 24L * 60L * 60L * 1000L
    private val startOfRange = nowZoned.minusDays(6).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

    val uiState = combine(
        transactionRepository.observeTotalAmountForType(TransactionType.INCOME),
        transactionRepository.observeTotalAmountForType(TransactionType.EXPENSE),
        transactionRepository.observeAmountBetween(TransactionType.EXPENSE, startOfToday, endOfToday),
        transactionRepository.observeCategoryTotalsBetween(TransactionType.EXPENSE, startOfRange, endOfToday),
        transactionRepository.observeCategoryTotalsBetween(TransactionType.INCOME, startOfRange, endOfToday)
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
}

