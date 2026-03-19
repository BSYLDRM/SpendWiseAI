package com.example.spendwiseai.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.example.spendwiseai.data.db.CategoryEntity

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): CategoryEntity?

    @Query("SELECT id FROM categories WHERE name = :name LIMIT 1")
    suspend fun findIdByName(name: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Query("SELECT * FROM categories ORDER BY name")
    suspend fun getAllCategories(): List<CategoryEntity>
}

