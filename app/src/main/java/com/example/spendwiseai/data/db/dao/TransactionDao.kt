package com.example.spendwiseai.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.spendwiseai.data.db.TransactionEntity
import com.example.spendwiseai.domain.model.TransactionType

data class CategoryTotal(
    val categoryName: String,
    val totalAmount: Double
)

data class TransactionWithCategory(
    val id: Long,
    val amount: Double,
    val currency: String,
    val categoryName: String,
    val description: String,
    val dateMillis: Long,
    val type: TransactionType
)

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity): Int

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = :type
        """
    )
    suspend fun getTotalAmountForType(type: TransactionType): Double

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = :type
        """
    )
    fun observeTotalAmountForType(type: TransactionType): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = :type AND dateMillis >= :startMillis AND dateMillis < :endMillis
        """
    )
    suspend fun getAmountBetween(type: TransactionType, startMillis: Long, endMillis: Long): Double

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE type = :type AND dateMillis >= :startMillis AND dateMillis < :endMillis
        """
    )
    fun observeAmountBetween(type: TransactionType, startMillis: Long, endMillis: Long): Flow<Double>

    @Query(
        """
        SELECT t.id,
               t.amount,
               t.currency,
               c.name AS categoryName,
               t.description,
               t.dateMillis,
               t.type
        FROM transactions t
        INNER JOIN categories c ON c.id = t.categoryId
        WHERE t.type = :type
        ORDER BY t.dateMillis DESC
        """
    )
    fun observeTransactions(type: TransactionType): Flow<List<TransactionWithCategory>>

    @Query(
        """
        SELECT t.id,
               t.amount,
               t.currency,
               c.name AS categoryName,
               t.description,
               t.dateMillis,
               t.type
        FROM transactions t
        INNER JOIN categories c ON c.id = t.categoryId
        WHERE t.id = :id
        """
    )
    suspend fun getTransactionById(id: Long): TransactionWithCategory?

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query(
        """
        SELECT c.name AS categoryName,
               COALESCE(SUM(t.amount), 0) AS totalAmount
        FROM transactions t
        INNER JOIN categories c ON c.id = t.categoryId
        WHERE t.type = :type AND t.dateMillis >= :startMillis AND t.dateMillis < :endMillis
        GROUP BY c.id
        ORDER BY totalAmount DESC
        """
    )
    suspend fun getCategoryTotalsBetween(
        type: TransactionType,
        startMillis: Long,
        endMillis: Long
    ): List<CategoryTotal>

    @Query(
        """
        SELECT c.name AS categoryName,
               COALESCE(SUM(t.amount), 0) AS totalAmount
        FROM transactions t
        INNER JOIN categories c ON c.id = t.categoryId
        WHERE t.type = :type AND t.dateMillis >= :startMillis AND t.dateMillis < :endMillis
        GROUP BY c.id
        ORDER BY totalAmount DESC
        """
    )
    fun observeCategoryTotalsBetween(
        type: TransactionType,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<CategoryTotal>>
}

