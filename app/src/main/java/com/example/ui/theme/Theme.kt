package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CalcKeyOperatorDark,
    onPrimary = Color.White,
    secondary = CalcKeyEqualsDark,
    onSecondary = Color.White,
    tertiary = CalcKeyActionDark,
    background = CalcBgDark,
    surface = CalcSurfaceDark,
    surfaceVariant = CalcSurfaceVariantDark,
    onBackground = CalcTextPrimaryDark,
    onSurface = CalcTextPrimaryDark,
    onSurfaceVariant = CalcTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = CalcKeyOperatorLight,
    onPrimary = Color.White,
    secondary = CalcKeyEqualsLight,
    onSecondary = Color.White,
    tertiary = CalcKeyActionLight,
    background = CalcBgLight,
    surface = CalcSurfaceLight,
    surfaceVariant = CalcSurfaceVariantLight,
    onBackground = CalcTextPrimaryLight,
    onSurface = CalcTextPrimaryLight,
    onSurfaceVariant = CalcTextSecondaryLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
