package com.example.spendwiseai.core

import android.content.Context
import androidx.room.Room
import com.example.spendwiseai.ai.gemini.GeminiExpenseTextParser
import com.example.spendwiseai.ai.gemini.GeminiInsightsGenerator
import com.example.spendwiseai.data.db.AppDatabase
import com.example.spendwiseai.data.repository.AuthRepository
import com.example.spendwiseai.data.repository.CategoryRepository
import com.example.spendwiseai.data.repository.FirestoreRepository
import com.example.spendwiseai.data.repository.InsightsRepository
import com.example.spendwiseai.data.repository.TransactionRepository
import com.example.spendwiseai.domain.usecase.AddExpenseUseCase
import com.example.spendwiseai.presentation.auth.LoginViewModelFactory

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

    val authRepository: AuthRepository by lazy { AuthRepository() }

    private val firestoreRepository: FirestoreRepository by lazy { FirestoreRepository() }

    private val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(
            transactionDao = database.transactionDao(),
            categoryRepository = categoryRepository,
            firestoreRepository = firestoreRepository
        )
    }

    private val insightsRepository: InsightsRepository by lazy {
        InsightsRepository(insightDao = database.insightDao())
    }

    private val addExpenseUseCaseLazy: AddExpenseUseCase by lazy {
        AddExpenseUseCase(
            parser = GeminiExpenseTextParser(apiKey = com.example.spendwiseai.BuildConfig.GEMINI_API_KEY),
            transactionRepository = transactionRepository
        )
    }

    fun getAddExpenseUseCase(): AddExpenseUseCase = addExpenseUseCaseLazy

    fun provideTransactionRepository(): TransactionRepository = transactionRepository

    private val insightsGenerator: GeminiInsightsGenerator by lazy {
        GeminiInsightsGenerator(apiKey = com.example.spendwiseai.BuildConfig.GEMINI_API_KEY)
    }

    fun provideInsightsRepository(): InsightsRepository = insightsRepository

    fun provideInsightsGenerator(): GeminiInsightsGenerator = insightsGenerator

    fun provideLoginViewModelFactory(): LoginViewModelFactory =
        LoginViewModelFactory(authRepository, transactionRepository)

    suspend fun clearAllLocalData() {
        transactionRepository.clearLocalData()
        insightsRepository.clearLocalData()
    }
}
