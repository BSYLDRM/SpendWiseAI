package com.example.spendwiseai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.spendwiseai.ui.theme.AppBackground
import com.example.spendwiseai.ui.theme.GlassSurface
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed
import com.example.spendwiseai.ui.theme.TertiaryDark

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = SoftCoralRed,
    tertiary = TertiaryDark,
    background = AppBackground,
    surface = GlassSurface,
)

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = NeonGreen,
    secondary = SoftCoralRed,
    tertiary = TertiaryDark,
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
)

@Composable
fun SpendWiseAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}