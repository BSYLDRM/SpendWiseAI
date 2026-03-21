package com.example.spendwiseai.presentation.dashboard

import com.example.spendwiseai.data.db.dao.CategoryTotal

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalBalance: Double = 0.0, // income - expenses
    val dailySpending: Double = 0.0,
    val expenseCategoryTotals: List<CategoryTotal> = emptyList(),
    val incomeCategoryTotals: List<CategoryTotal> = emptyList()
)

