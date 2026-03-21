package com.example.spendwiseai.presentation.budget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendwiseai.data.repository.TransactionRepository

class BudgetViewModelFactory(
    private val context: Context,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            return BudgetViewModel(context, transactionRepository) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
