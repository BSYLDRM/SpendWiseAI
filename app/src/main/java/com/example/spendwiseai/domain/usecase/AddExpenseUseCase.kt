package com.example.spendwiseai.domain.usecase

import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.ai.ExpenseTextParser
import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType

data class AddExpenseResult(
    val transactionId: Long,
    val parsed: ParsedTransaction
)

class AddExpenseUseCase(
    private val parser: ExpenseTextParser,
    private val transactionRepository: TransactionRepository
) {
    // Sadece parse eder, DB'ye yazmaz
    suspend fun parse(
        userText: String,
        forcedType: TransactionType? = null
    ): ParsedTransaction {
        val parsed = parser.parseExpense(userText)
        return if (forcedType != null) parsed.copy(type = forcedType) else parsed
    }

    // Kullanıcı onaylayınca çağrılır, DB'ye yazar
    suspend fun save(
        parsed: ParsedTransaction,
        userText: String,
        dateMillis: Long = System.currentTimeMillis()
    ): AddExpenseResult {
        val id = transactionRepository.addTransaction(
            transaction = parsed,
            description = userText,
            dateMillis = dateMillis
        )
        return AddExpenseResult(transactionId = id, parsed = parsed)
    }

    // Eski kod uyumluluğu için — direkt parse + kayıt
    suspend fun execute(
        userText: String,
        forcedType: TransactionType? = null,
        dateMillis: Long = System.currentTimeMillis()
    ): AddExpenseResult {
        val parsed = parse(userText, forcedType)
        return save(parsed, userText, dateMillis)
    }
}