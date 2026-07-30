package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ReadingPrimaryLight,
    onPrimary = ReadingOnPrimaryLight,
    primaryContainer = ReadingPrimaryContainerLight,
    onPrimaryContainer = ReadingOnPrimaryContainerLight,
    secondary = ReadingSecondaryLight,
    onSecondary = ReadingOnSecondaryLight,
    secondaryContainer = ReadingSecondaryContainerLight,
    onSecondaryContainer = ReadingOnSecondaryContainerLight,
    tertiary = ReadingTertiaryLight,
    tertiaryContainer = ReadingTertiaryContainerLight,
    background = ReadingBackgroundLight,
    onBackground = ReadingOnBackgroundLight,
    surface = ReadingSurfaceLight,
    onSurface = ReadingOnBackgroundLight,
    surfaceVariant = ReadingMutedCardLight,
    onSurfaceVariant = ReadingSecondaryTextLight,
    outline = ReadingBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = ReadingPrimaryDark,
    onPrimary = ReadingOnPrimaryDark,
    primaryContainer = ReadingPrimaryContainerDark,
    onPrimaryContainer = ReadingOnPrimaryContainerDark,
    secondary = ReadingSecondaryDark,
    onSecondary = ReadingOnSecondaryDark,
    secondaryContainer = ReadingSecondaryContainerDark,
    onSecondaryContainer = ReadingOnSecondaryContainerDark,
    tertiary = ReadingTertiaryDark,
    tertiaryContainer = ReadingTertiaryContainerDark,
    background = ReadingBackgroundDark,
    onBackground = ReadingOnBackgroundDark,
    surface = ReadingSurfaceDark,
    onSurface = ReadingOnBackgroundDark,
    surfaceVariant = ReadingMutedCardDark,
    onSurfaceVariant = ReadingSecondaryTextDark,
    outline = ReadingBorderDark
)

@Composable
fun PlanoBiblicoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our reading-optimized warm paper / night palette
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

