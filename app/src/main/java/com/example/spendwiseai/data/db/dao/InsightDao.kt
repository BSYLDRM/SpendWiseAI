package com.example.spendwiseai.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spendwiseai.data.db.InsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    @Query("SELECT * FROM insights ORDER BY weekStartMillis DESC")
    fun observeAll(): Flow<List<InsightEntity>>

    @Query("SELECT * FROM insights WHERE weekStartMillis = :weekStartMillis LIMIT 1")
    suspend fun findByWeekStart(weekStartMillis: Long): InsightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(insight: InsightEntity)
}

