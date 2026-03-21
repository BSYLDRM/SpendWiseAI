package com.example.spendwiseai.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType
import com.example.spendwiseai.domain.usecase.AddExpenseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _selectedType = MutableStateFlow(TransactionType.EXPENSE)
    val selectedType = _selectedType.asStateFlow()

    private var pendingText: String = ""

    fun onTypeChanged(type: TransactionType) {
        _selectedType.value = type
        // Tip değişince preview ve kayıt sıfırlanır
        _uiState.value = _uiState.value.copy(
            preview = null,
            lastTransactionId = null,
            errorMessage = null
        )
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
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        pendingText = text

        _uiState.value = _uiState.value.copy(
            isSubmitting = true,
            errorMessage = null,
            preview = null,
            lastTransactionId = null
        )

        viewModelScope.launch {
            try {
                val parsed = addExpenseUseCase.parse(
                    userText = text,
                    forcedType = _selectedType.value
                )
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    preview = parsed,
                    inputText = ""
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = t.message ?: "Analiz başarısız",
                    preview = null
                )
            }
        }
    }

    fun confirmSave() {
        val preview = _uiState.value.preview ?: return

        viewModelScope.launch {
            try {
                val result = addExpenseUseCase.save(
                    parsed = preview,
                    userText = pendingText
                )
                _uiState.value = _uiState.value.copy(
                    lastTransactionId = result.transactionId
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = t.message ?: "Kayıt başarısız"
                )
            }
        }
    }
}