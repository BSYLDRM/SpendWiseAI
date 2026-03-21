package com.example.spendwiseai.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.model.TransactionType
import com.example.spendwiseai.domain.usecase.AddExpenseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()
    private val _selectedType = MutableStateFlow(TransactionType.EXPENSE)
    val selectedType = _selectedType.asStateFlow()

    fun onTypeChanged(type: TransactionType) {
        _selectedType.value = type
    }

    fun onInputChanged(newText: String) {
        _uiState.value = _uiState.value.copy(
            inputText = newText,
            errorMessage = null,
            preview = null,
            lastTransactionId = null
        )
    }

    fun submit() {
        val state = _uiState.value
        val text = state.inputText.trim()
        if (text.isBlank()) return

        _uiState.value = _uiState.value.copy(
            isSubmitting = true,
            errorMessage = null,
            preview = null,
            lastTransactionId = null
        )

        viewModelScope.launch {
            try {
                val result = addExpenseUseCase.execute(userText = text)
                val correctedParsed = result.parsed.copy(type = _selectedType.value)
                val existing = transactionRepository.getTransactionById(result.transactionId)
                if (existing != null) {
                    transactionRepository.updateTransaction(
                        id = existing.id,
                        amount = existing.amount,
                        currency = existing.currency,
                        categoryName = existing.categoryName,
                        description = existing.description,
                        dateMillis = existing.dateMillis,
                        type = _selectedType.value
                    )
                }
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    preview = correctedParsed,
                    lastTransactionId = result.transactionId,
                    inputText = ""
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = t.message ?: "Failed to add expense",
                    preview = null
                )
            }
        }
    }
}

