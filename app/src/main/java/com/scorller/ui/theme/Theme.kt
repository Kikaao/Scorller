package com.scorller.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Coral,
    onPrimary = White,
    primaryContainer = CoralLight,
    onPrimaryContainer = InkOnCoralContainer,
    secondary = InkTeal,
    onSecondary = White,
    secondaryContainer = InkTealLight,
    onSecondaryContainer = White,
    tertiary = Gold,
    onTertiary = InkTeal,
    tertiaryContainer = GoldLight,
    onTertiaryContainer = InkTeal,
    background = Cream,
    onBackground = InkTeal,
    surface = CreamSurface,
    onSurface = InkTeal,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = InkTealLight,
    outline = CreamOutline,
    error = NopeRed,
    onError = White
)

private val DarkColors = darkColorScheme(
    primary = CoralLight,
    onPrimary = CoralDark,
    primaryContainer = CoralDark,
    onPrimaryContainer = CoralLight,
    secondary = InkTealBright,
    onSecondary = CharcoalBg,
    secondaryContainer = InkTealLight,
    onSecondaryContainer = White,
    tertiary = GoldLight,
    onTertiary = CharcoalBg,
    tertiaryContainer = Gold,
    onTertiaryContainer = CharcoalBg,
    background = CharcoalBg,
    onBackground = Cream,
    surface = CharcoalSurface,
    onSurface = Cream,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = CreamOutline,
    outline = CharcoalOutline,
    error = NopeRed,
    onError = White
)

@Composable
fun ScorllerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ScorllerTypography,
        shapes = ScorllerShapes,
        content = content
    )
}
