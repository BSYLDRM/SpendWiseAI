package com.example.spendwiseai.ai.gemini

import com.example.spendwiseai.domain.ai.ExpenseTextParser
import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType

class RegexExpenseTextParser : ExpenseTextParser {
    override suspend fun parseExpense(text: String): ParsedTransaction {
        val normalized = text.trim()
        require(normalized.isNotEmpty()) { "text cannot be blank" }

        val amount = extractAmount(normalized)
        val currency = extractCurrency(normalized)
        val category = inferCategory(normalized)
        val type = inferType(normalized)

        return ParsedTransaction(
            amount = amount,
            currency = currency,
            category = category,
            type = type
        )
    }

    private fun extractAmount(text: String): Double {
        // Matches "150", "150.50", "150,50".
        val regex = Regex("""-?\d+(?:[.,]\d+)?""")
        val match = regex.find(text) ?: error("Could not find an amount in: '$text'")
        val raw = match.value
        return raw.replace(',', '.').toDouble()
    }

    private fun extractCurrency(text: String): String {
        val upper = text.uppercase()

        return when {
            upper.contains("₺") || upper.contains(" TL") || Regex("""\bTL\b""").containsMatchIn(upper) -> "TL"
            upper.contains(" TRY") || upper.contains("TRY") -> "TRY"
            upper.contains("€") || upper.contains(" EUR") || upper.contains("EUR") -> "EUR"
            upper.contains("$") || upper.contains(" USD") || upper.contains("USD") -> "USD"
            upper.contains("£") || upper.contains(" GBP") || upper.contains("GBP") -> "GBP"
            else -> "UNK"
        }
    }

    private fun inferCategory(text: String): String {
        val lower = text.lowercase()
        return when {
            listOf(
                "restoran", "lokanta", "restaurant", "cafe", "kahve", "coffee", "starbucks",
                "yemek", "doner", "doner", "burger", "pizza", "icecek", "içecek", "drink", "latte"
            ).any { lower.contains(it) } ->
                "Food & Drink"
            listOf(
                "migros", "bim", "a101", "sok", "şok", "carrefour", "market", "manav",
                "ekmek", "sut", "süt", "sebze", "meyve", "alisveris", "alışveriş",
                "grocery", "supermarket", "groceries"
            ).any { lower.contains(it) } -> "Groceries"
            listOf(
                "benzin", "motorin", "akaryakit", "akaryakıt", "opet", "shell", "bp",
                "petrol", "otobus", "otobüs", "metro", "taksi", "uber", "dolmus", "dolmuş",
                "bus", "train", "transport"
            ).any { lower.contains(it) } -> "Transportation"
            listOf(
                "bar", "pub", "gece", "konser", "sinema", "netflix", "spotify", "oyun",
                "eglence", "eğlence", "movie", "cinema", "game", "concert"
            ).any { lower.contains(it) } -> "Entertainment"
            listOf("shop", "clothes", "clothing", "store", "shopping").any { lower.contains(it) } -> "Shopping"
            listOf(
                "elektrik", "su", "dogalgaz", "doğalgaz", "internet", "fatura", "aidat",
                "electric", "water", "bill", "utility"
            ).any { lower.contains(it) } -> "Bills & Utilities"
            listOf("rent", "landlord", "apartment").any { lower.contains(it) } -> "Rent"
            listOf(
                "eczane", "doktor", "hastane", "ilac", "ilaç", "muayene",
                "doctor", "pharmacy", "health"
            ).any { lower.contains(it) } -> "Health"
            listOf("school", "education", "course").any { lower.contains(it) } -> "Education"
            listOf("travel", "flight", "hotel", "trip").any { lower.contains(it) } -> "Travel"
            else -> "Other"
        }
    }

    private fun inferType(text: String): TransactionType {
        val lower = text.lowercase()
        return when {
            listOf("salary", "income", "received", "payment received", "paycheck", "refund", "returned", "sold").any {
                lower.contains(it)
            } -> TransactionType.INCOME
            listOf("spent", "expense", "paid", "buy", "bought", "shopping").any { lower.contains(it) } ->
                TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }
    }
}

