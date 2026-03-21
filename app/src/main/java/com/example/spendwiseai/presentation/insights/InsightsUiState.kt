package com.example.spendwiseai.presentation.insights

import com.example.spendwiseai.data.db.InsightEntity

data class InsightsUiState(
    val isLoading: Boolean = true,
    val insights: List<InsightEntity> = emptyList()
)

