package com.example.spendwiseai.core

import android.content.Context

object BudgetManager {
    private const val PREF_NAME = "smart_spend_prefs"

    fun saveBudget(context: Context, month: String, amount: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat("budget_$month", amount.toFloat()).apply()
    }

    fun getBudget(context: Context, month: String): Double {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat("budget_$month", 0f).toDouble()
    }
}
