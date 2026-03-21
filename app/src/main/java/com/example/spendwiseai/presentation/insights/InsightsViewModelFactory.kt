package com.example.spendwiseai.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendwiseai.ai.gemini.GeminiInsightsGenerator
import com.example.spendwiseai.data.repository.InsightsRepository
import com.example.spendwiseai.data.repository.TransactionRepository

class InsightsViewModelFactory(
    private val insightsRepository: InsightsRepository,
    private val transactionRepository: TransactionRepository,
    private val generator: GeminiInsightsGenerator
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(InsightsViewModel::class.java) ->
                InsightsViewModel(
                    insightsRepository = insightsRepository,
                    transactionRepository = transactionRepository,
                    generator = generator
                ) as T

            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

