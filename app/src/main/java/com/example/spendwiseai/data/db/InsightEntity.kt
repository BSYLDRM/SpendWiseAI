package com.example.spendwiseai.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "insights",
    indices = [
        Index(value = ["weekStartMillis"], unique = true)
    ]
)
data class InsightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val weekStartMillis: Long,
    val content: String,
    val createdAtMillis: Long
)

