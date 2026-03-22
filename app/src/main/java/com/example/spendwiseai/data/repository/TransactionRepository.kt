package com.example.spendwiseai.data.repository

import com.example.spendwiseai.data.db.TransactionEntity
import com.example.spendwiseai.data.db.dao.TransactionDao
import com.example.spendwiseai.data.db.dao.TransactionWithCategory
import com.example.spendwiseai.domain.model.ParsedTransaction
import com.example.spendwiseai.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository,
    private val firestoreRepository: FirestoreRepository
) {
    suspend fun addTransaction(
        transaction: ParsedTransaction,
        description: String,
        dateMillis: Long
    ): Long {
        val categoryId = categoryRepository.getOrCreateCategoryId(transaction.category)
        val id = transactionDao.insert(
            TransactionEntity(
                amount = transaction.amount,
                currency = transaction.currency,
                categoryId = categoryId,
                description = description.trim(),
                dateMillis = dateMillis,
                type = transaction.type
            )
        )
        runCatching { firestoreRepository.upsertTransaction(id, transaction.amount, transaction.currency, transaction.category, description.trim(), dateMillis, transaction.type) }
        return id
    }

    fun observeTransactions(type: TransactionType): Flow<List<TransactionWithCategory>> {
        return transactionDao.observeTransactions(type)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
        runCatching { firestoreRepository.deleteTransaction(id) }
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
        runCatching { firestoreRepository.upsertTransaction(id, amount, currency, categoryName, description.trim(), dateMillis, type) }
    }

    suspend fun getTotalAmountForType(type: TransactionType): Double {
        return transactionDao.getTotalAmountForType(type)
    }

    fun observeTotalAmountForType(type: TransactionType): Flow<Double> {
        return transactionDao.observeTotalAmountForType(type)
    }

    suspend fun getAmountBetween(type: TransactionType, startMillis: Long, endMillis: Long): Double {
        return transactionDao.getAmountBetween(type, startMillis, endMillis)
    }

    fun observeAmountBetween(type: TransactionType, startMillis: Long, endMillis: Long): Flow<Double> {
        return transactionDao.observeAmountBetween(type, startMillis, endMillis)
    }

    suspend fun getCategoryTotalsBetween(
        type: TransactionType,
        startMillis: Long,
        endMillis: Long
    ): List<com.example.spendwiseai.data.db.dao.CategoryTotal> {
        return transactionDao.getCategoryTotalsBetween(type, startMillis, endMillis)
    }

    fun observeCategoryTotalsBetween(
        type: TransactionType,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<com.example.spendwiseai.data.db.dao.CategoryTotal>> {
        return transactionDao.observeCategoryTotalsBetween(type, startMillis, endMillis)
    }

    suspend fun syncFromFirestore() {
        transactionDao.deleteAll()
        val remote = firestoreRepository.fetchAllTransactions()
        remote.forEach { tx ->
            val categoryId = categoryRepository.getOrCreateCategoryId(tx.categoryName)
            transactionDao.insert(TransactionEntity(
                id = tx.id, amount = tx.amount, currency = tx.currency,
                categoryId = categoryId, description = tx.description,
                dateMillis = tx.dateMillis, type = tx.type
            ))
        }
    }

    suspend fun clearLocalData() {
        transactionDao.deleteAll()
    }
}
