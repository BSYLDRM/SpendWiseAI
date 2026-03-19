package com.example.spendwiseai.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.model.TransactionType

class TransactionsListViewModelFactory(
    private val transactionType: TransactionType,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TransactionsListViewModel::class.java) ->
                TransactionsListViewModel(
                    transactionType = transactionType,
                    transactionRepository = transactionRepository
                ) as T
            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

