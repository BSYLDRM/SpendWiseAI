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
        val regex = Regex("""-?\d+(?:[.,]\d+)?""")
        val match = regex.find(text) ?: error("Could not find an amount in: '$text'")
        return match.value.replace(',', '.').toDouble()
    }

    private fun extractCurrency(text: String): String {
        val upper = text.uppercase()
        return when {
            upper.contains("₺") || upper.contains(" TL") ||
                    Regex("""\bTL\b""").containsMatchIn(upper) -> "TL"
            upper.contains("TRY") -> "TRY"
            upper.contains("€") || upper.contains("EUR") -> "EUR"
            upper.contains("$") || upper.contains("USD") -> "USD"
            upper.contains("£") || upper.contains("GBP") -> "GBP"
            else -> "TL"
        }
    }

    private fun inferCategory(text: String): String {
        val lower = text.lowercase()

        // Gelir kategorileri
        if (inferType(text) == TransactionType.INCOME) {
            return when {
                listOf("maaş", "ikramiye", "prim", "aylık").any { lower.contains(it) } -> "Salary"
                listOf("freelance", "proje", "danışmanlık").any { lower.contains(it) } -> "Freelance"
                listOf("iade", "geri ödeme", "cashback").any { lower.contains(it) } -> "Refund"
                listOf("yemek parası", "yemek kartı", "yemek yardımı").any { lower.contains(it) } -> "Meal Allowance"
                listOf("faiz", "temettü", "yatırım").any { lower.contains(it) } -> "Investment"
                listOf("hediye", "bağış", "harçlık").any { lower.contains(it) } -> "Gift"
                else -> "Other Income"
            }
        }

        // Gider kategorileri — sıralama önemli, önce spesifik olanlar
        return when {
            // Kira
            listOf("kira", "ev kirası", "konut kirası").any { lower.contains(it) } ->
                "Rent"

            // Food & Drink — alkol dahil
            listOf(
                "bira", "alkol", "içki", "şarap", "viski", "rakı", "votka", "cin",
                "kokteyl", "bar", "pub", "restoran", "lokanta", "cafe", "kahve",
                "starbucks", "mcdonalds", "kfc", "burger", "pizza", "döner",
                "yemek", "içecek", "meşrubat"
            ).any { lower.contains(it) } -> "Food & Drink"

            // Groceries
            listOf(
                "migros", "bim", "a101", "şok", "carrefour", "market", "manav",
                "ekmek", "süt", "yumurta", "sebze", "meyve", "et", "tavuk",
                "balık", "peynir", "gıda", "erzak"
            ).any { lower.contains(it) } -> "Groceries"

            // Transportation — mazot dahil
            listOf(
                "benzin", "motorin", "mazot", "akaryakıt", "yakıt",
                "opet", "shell", "bp", "total", "petrol",
                "otobüs", "metro", "taksi", "uber", "dolmuş",
                "uçak", "tren", "vapur", "otogar", "otopark",
                "lastik", "oto yıkama", "araç"
            ).any { lower.contains(it) } -> "Transportation"

            // Health
            listOf(
                "eczane", "ilaç", "doktor", "hastane", "muayene",
                "diş", "tahlil", "reçete", "vitamin", "spor salonu"
            ).any { lower.contains(it) } -> "Health"

            // Bills & Utilities
            listOf(
                "elektrik", "su faturası", "doğalgaz", "internet faturası",
                "telefon faturası", "fatura", "aidat", "sigorta", "vergi"
            ).any { lower.contains(it) } -> "Bills & Utilities"

            // Entertainment
            listOf(
                "sinema", "konser", "tiyatro", "netflix", "spotify",
                "bilet", "eğlence", "otel", "tatil", "müze"
            ).any { lower.contains(it) } -> "Entertainment"

            // Technology
            listOf(
                "bilgisayar", "laptop", "telefon", "tablet", "kulaklık",
                "elektronik", "yazılım", "oyun", "steam", "apple", "samsung"
            ).any { lower.contains(it) } -> "Technology"

            // Shopping
            listOf(
                "kıyafet", "ayakkabı", "çanta", "zara", "lcw", "h&m",
                "mango", "koton", "mağaza", "avm", "trendyol", "hepsiburada"
            ).any { lower.contains(it) } -> "Shopping"

            // Education
            listOf(
                "kitap", "kurs", "eğitim", "okul", "ders",
                "üniversite", "sınav", "udemy"
            ).any { lower.contains(it) } -> "Education"

            else -> "Other"
        }
    }

    private fun inferType(text: String): TransactionType {
        val lower = text.lowercase()
        return when {
            listOf(
                "maaş", "geldi", "kazandım", "aldım", "ikramiye", "prim",
                "iade", "freelance", "yemek parası", "salary", "income",
                "received", "refund", "returned"
            ).any { lower.contains(it) } -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }
    }
}