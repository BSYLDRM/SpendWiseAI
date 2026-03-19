package com.example.spendwiseai.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionsListViewModel(
    private val transactionType: TransactionType,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState = transactionRepository
        .observeTransactions(transactionType)
        .map { TransactionsListUiState(transactions = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionsListUiState()
        )

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }

    fun updateTransaction(
        id: Long,
        amount: Double,
        currency: String,
        categoryName: String,
        description: String,
        dateMillis: Long
    ) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(
                id = id,
                amount = amount,
                currency = currency,
                categoryName = categoryName,
                description = description,
                dateMillis = dateMillis,
                type = transactionType
            )
        }
    }
}

