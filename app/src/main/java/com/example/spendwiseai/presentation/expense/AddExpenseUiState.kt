package com.example.spendwiseai.presentation.expense

import com.example.spendwiseai.domain.model.ParsedTransaction

data class AddExpenseUiState(
    val inputText: String = "",
    val isSubmitting: Boolean = false,
    val preview: ParsedTransaction? = null,
    val errorMessage: String? = null,
    val lastTransactionId: Long? = null
)

