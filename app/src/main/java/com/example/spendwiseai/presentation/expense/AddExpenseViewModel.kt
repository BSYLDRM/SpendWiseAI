package com.example.spendwiseai.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwiseai.domain.usecase.AddExpenseUseCase
import com.example.spendwiseai.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun onInputChanged(newText: String) {
        _uiState.value = _uiState.value.copy(
            inputText = newText,
            errorMessage = null,
            preview = null,
            lastTransactionId = null
        )
    }

    fun onTypeSelected(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            errorMessage = null
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
                val result = addExpenseUseCase.execute(
                    userText = text,
                    forcedType = state.selectedType
                )
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    preview = result.parsed,
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

