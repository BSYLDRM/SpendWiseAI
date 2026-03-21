package com.example.spendwiseai.presentation.expense

import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType

data class AddExpenseUiState(
    val inputText: String = "",
    val isSubmitting: Boolean = false,
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val preview: ParsedTransaction? = null,
    val errorMessage: String? = null,
    val lastTransactionId: Long? = null
)

