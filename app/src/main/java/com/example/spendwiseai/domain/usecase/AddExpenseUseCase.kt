package com.example.spendwiseai.domain.usecase

import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.ai.ExpenseTextParser
import com.example.spendwiseai.domain.model.ParsedTransaction

data class AddExpenseResult(
    val transactionId: Long,
    val parsed: ParsedTransaction
)

class AddExpenseUseCase(
    private val parser: ExpenseTextParser,
    private val transactionRepository: TransactionRepository
) {
    suspend fun execute(
        userText: String,
        dateMillis: Long = System.currentTimeMillis()
    ): AddExpenseResult {
        val parsed = parser.parseExpense(userText)
        val id = transactionRepository.addTransaction(
            transaction = parsed,
            description = userText,
            dateMillis = dateMillis
        )
        return AddExpenseResult(transactionId = id, parsed = parsed)
    }
}

