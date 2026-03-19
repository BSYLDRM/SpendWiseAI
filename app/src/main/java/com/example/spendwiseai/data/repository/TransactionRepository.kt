package com.example.spendwiseai.data.repository

import com.example.spendwiseai.data.db.TransactionEntity
import com.example.spendwiseai.data.db.dao.TransactionDao
import com.example.spendwiseai.data.db.dao.TransactionWithCategory
import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository
) {
    suspend fun addTransaction(
        transaction: ParsedTransaction,
        description: String,
        dateMillis: Long
    ): Long {
        val categoryId = categoryRepository.getOrCreateCategoryId(transaction.category)
        return transactionDao.insert(
            TransactionEntity(
                amount = transaction.amount,
                currency = transaction.currency,
                categoryId = categoryId,
                description = description.trim(),
                dateMillis = dateMillis,
                type = transaction.type
            )
        )
    }

    fun observeTransactions(type: TransactionType): Flow<List<TransactionWithCategory>> {
        return transactionDao.observeTransactions(type)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun getTransactionById(id: Long): TransactionWithCategory? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun updateTransaction(
        id: Long,
        amount: Double,
        currency: String,
        categoryName: String,
        description: String,
        dateMillis: Long,
        type: TransactionType
    ) {
        val categoryId = categoryRepository.getOrCreateCategoryId(categoryName)
        transactionDao.update(
            TransactionEntity(
                id = id,
                amount = amount,
                currency = currency,
                categoryId = categoryId,
                description = description.trim(),
                dateMillis = dateMillis,
                type = type
            )
        )
    }
}

