package com.example.spendwiseai.presentation.scan

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.ai.ReceiptVisionParser
import com.example.spendwiseai.domain.model.ParsedTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScanReceiptViewModel(
    private val visionParser: ReceiptVisionParser,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanReceiptUiState())
    val uiState: StateFlow<ScanReceiptUiState> = _uiState.asStateFlow()

    fun onPhotoCaptured(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            capturedBitmap = bitmap,
            isParsing = true,
            preview = null,
            errorMessage = null,
            savedTransactionId = null
        )

        viewModelScope.launch {
            try {
                // Ensure the UI recomposes and shows the "Analyzing receipt" overlay,
                // even if the parser fails immediately (e.g., missing API key).
                delay(200)
                val parsed = visionParser.parseReceipt(bitmap)
                _uiState.value = _uiState.value.copy(
                    isParsing = false,
                    preview = parsed
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isParsing = false,
                    errorMessage = t.message ?: "Failed to parse receipt."
                )
            }
        }
    }

    fun clearError() {
        val current = _uiState.value
        if (current.errorMessage == null) return
        _uiState.value = current.copy(errorMessage = null)
    }

    fun reset() {
        _uiState.value = ScanReceiptUiState()
    }

    fun confirmSave(parsed: ParsedTransaction) {
        val bitmap = _uiState.value.capturedBitmap
        if (bitmap == null) return

        viewModelScope.launch {
            val id = transactionRepository.addTransaction(
                transaction = parsed,
                description = "Receipt",
                dateMillis = System.currentTimeMillis()
            )
            _uiState.value = _uiState.value.copy(savedTransactionId = id)
        }
    }
}

