package com.example.spendwiseai.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.ai.ReceiptVisionParser

class ScanReceiptViewModelFactory(
    private val visionParser: ReceiptVisionParser,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ScanReceiptViewModel::class.java) ->
                ScanReceiptViewModel(
                    visionParser = visionParser,
                    transactionRepository = transactionRepository
                ) as T
            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

