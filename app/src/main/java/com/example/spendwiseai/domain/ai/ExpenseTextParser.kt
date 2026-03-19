package com.example.spendwiseai.domain.ai

import com.example.spendwiseai.domain.model.ParsedTransaction

interface ExpenseTextParser {
    /**
     * Parse a single expense/income from free-form user text.
     * Implementations should throw an exception for non-recoverable failures.
     */
    suspend fun parseExpense(text: String): ParsedTransaction
}

