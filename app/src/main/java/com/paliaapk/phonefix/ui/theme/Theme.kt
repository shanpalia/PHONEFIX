package com.paliaapk.phonefix.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlue,
    onPrimary = BrandWhite,
    primaryContainer = BrandNavyCardLight,
    onPrimaryContainer = BrandCyanLight,
    secondary = BrandCyan,
    onSecondary = BrandNavyDark,
    secondaryContainer = BrandNavyCard,
    onSecondaryContainer = BrandCyan,
    tertiary = BrandCyanLight,
    onTertiary = BrandNavyDark,
    background = BrandNavyDark,
    onBackground = BrandTextPrimary,
    surface = BrandNavySurface,
    onSurface = BrandTextPrimary,
    surfaceVariant = BrandNavyCard,
    onSurfaceVariant = BrandTextSecondary,
    outline = BrandNavyBorder,
    error = StatusCritical,
    onError = BrandWhite
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = BrandWhite,
    primaryContainer = BrandBlueLight.copy(alpha = 0.15f),
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandCyanDark,
    onSecondary = BrandWhite,
    secondaryContainer = BrandCyanLight.copy(alpha = 0.2f),
    onSecondaryContainer = BrandNavyDark,
    tertiary = BrandCyanMuted,
    onTertiary = BrandWhite,
    background = BrandOffWhite,
    onBackground = BrandNavyDark,
    surface = BrandWhite,
    onSurface = BrandNavyDark,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1),
    error = StatusCritical,
    onError = BrandWhite
)

@Composable
fun PhoneFixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We emphasize the sleek dark navy & cyan diagnostic tech aesthetic by default
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.White.toArgb()
                window.navigationBarColor = Color.White.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
