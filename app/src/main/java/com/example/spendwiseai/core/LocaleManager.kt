package com.example.spendwiseai.core

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {
    private const val PREFS_NAME = "smart_spend_prefs"
    private const val KEY_LANG = "lang" // "en" | "tr"
    private const val KEY_CURRENCY = "currency" // "TL" | "USD" | "EUR"

    fun getLanguageTag(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "en") ?: "en"
    }

    fun setLanguageTag(context: Context, languageTag: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, languageTag).apply()
    }

    fun getCurrency(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENCY, "TL") ?: "TL"
    }

    fun setCurrency(context: Context, currency: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun wrap(context: Context): Context {
        val languageTag = getLanguageTag(context)
        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}

