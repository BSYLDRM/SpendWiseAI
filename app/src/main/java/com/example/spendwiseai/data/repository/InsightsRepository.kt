package com.example.spendwiseai.data.repository

import com.example.spendwiseai.data.db.dao.InsightDao
import com.example.spendwiseai.data.db.InsightEntity
import kotlinx.coroutines.flow.Flow

class InsightsRepository(
    private val insightDao: InsightDao
) {
    fun observeAll(): Flow<List<InsightEntity>> = insightDao.observeAll()

    suspend fun findByWeekStart(weekStartMillis: Long): InsightEntity? {
        return insightDao.findByWeekStart(weekStartMillis)
    }

    suspend fun upsert(insight: InsightEntity) {
        insightDao.upsert(insight)
    }
}

