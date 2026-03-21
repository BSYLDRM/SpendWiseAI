package com.example.spendwiseai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendwiseai.domain.usecase.AddExpenseUseCase
import com.example.spendwiseai.presentation.expense.AddExpenseViewModel

class SpendWiseViewModelFactory(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddExpenseViewModel::class.java) ->
                AddExpenseViewModel(addExpenseUseCase) as T
            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}