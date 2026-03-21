package com.example.spendwiseai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.usecase.AddExpenseUseCase
import com.example.spendwiseai.presentation.expense.AddExpenseViewModel

class SpendWiseViewModelFactory(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddExpenseViewModel::class.java) ->
                AddExpenseViewModel(addExpenseUseCase, transactionRepository) as T
            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

