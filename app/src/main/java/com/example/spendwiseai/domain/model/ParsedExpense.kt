package com.example.spendwiseai.domain.model

/**
 * Result of parsing natural-language expense/income text.
 *
 * Note: category is a human-readable label (e.g., "Food & Drink") and will be resolved to
 * a database category row at save time.
 */
data class ParsedTransaction(
    val amount: Double,
    val currency: String,
    val category: String,
    val type: TransactionType
)

