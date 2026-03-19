package com.example.spendwiseai.core

import android.content.Context
import androidx.room.Room
import com.example.spendwiseai.ai.gemini.GeminiExpenseTextParser
import com.example.spendwiseai.data.db.AppDatabase
import com.example.spendwiseai.data.repository.CategoryRepository
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.usecase.AddExpenseUseCase

class AppContainer(context: Context) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "spendwiseai.db"
        ).fallbackToDestructiveMigration().build()
    }

    private val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    private val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(
            transactionDao = database.transactionDao(),
            categoryRepository = categoryRepository
        )
    }

    private val addExpenseUseCaseLazy: AddExpenseUseCase by lazy {
        AddExpenseUseCase(
            parser = GeminiExpenseTextParser(apiKey = com.example.spendwiseai.BuildConfig.GEMINI_API_KEY),
            transactionRepository = transactionRepository
        )
    }

    fun getAddExpenseUseCase(): AddExpenseUseCase = addExpenseUseCaseLazy

    fun getTransactionRepository(): TransactionRepository = transactionRepository
}

