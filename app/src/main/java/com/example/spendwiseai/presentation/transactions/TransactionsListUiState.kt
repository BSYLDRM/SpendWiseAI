package com.example.spendwiseai.presentation.transactions

import com.example.spendwiseai.data.db.dao.TransactionWithCategory

data class TransactionsListUiState(
    val transactions: List<TransactionWithCategory> = emptyList(),
    val errorMessage: String? = null
)

