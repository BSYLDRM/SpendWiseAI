package com.example.spendwiseai.data.repository

import com.example.spendwiseai.data.db.CategoryEntity
import com.example.spendwiseai.data.db.dao.CategoryDao

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    suspend fun getOrCreateCategoryId(categoryName: String): Long {
        val trimmed = categoryName.trim()
        require(trimmed.isNotEmpty()) { "categoryName cannot be blank" }

        val existingId = categoryDao.findIdByName(trimmed)
        if (existingId != null) return existingId

        val insertedId = categoryDao.insert(CategoryEntity(name = trimmed))
        return if (insertedId != -1L) insertedId else {
            // Another transaction may have created it in the meantime.
            categoryDao.findIdByName(trimmed) ?: error("Failed to resolve categoryId for '$trimmed'")
        }
    }
}

