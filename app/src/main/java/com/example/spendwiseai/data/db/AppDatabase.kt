package com.example.spendwiseai.data.db

import androidx.room.Database
import androidx.room.TypeConverters
import androidx.room.RoomDatabase

import com.example.spendwiseai.data.db.dao.CategoryDao
import com.example.spendwiseai.data.db.dao.TransactionDao

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(TransactionTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}

