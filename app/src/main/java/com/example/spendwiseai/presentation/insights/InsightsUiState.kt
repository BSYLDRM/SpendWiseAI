package com.example.spendwiseai.presentation.insights

import com.example.spendwiseai.data.db.InsightEntity

data class InsightsUiState(
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val insights: List<InsightEntity> = emptyList()
)

