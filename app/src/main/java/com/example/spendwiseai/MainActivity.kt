package com.example.spendwiseai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.spendwiseai.core.LocaleManager
import com.example.spendwiseai.ui.SpendWiseApp
import com.example.spendwiseai.ui.theme.SpendWiseAITheme

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendWiseAITheme {
                SpendWiseApp()
            }
        }
    }
}