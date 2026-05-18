package com.macbridge.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── MacBridge Color Palette ──
// A curated indigo-midnight palette for OLED-friendly dark theme.

private val md_primary = Color(0xFF7B8CDE)
private val md_onPrimary = Color(0xFF000000)
private val md_primaryContainer = Color(0xFF1a1a3a)
private val md_onPrimaryContainer = Color(0xFFCCDDFF)

private val md_secondary = Color(0xFF9AADFF)
private val md_onSecondary = Color(0xFF000000)
private val md_secondaryContainer = Color(0xFF111133)
private val md_onSecondaryContainer = Color(0xFFBBCCFF)

private val md_tertiary = Color(0xFFE8B931)
private val md_onTertiary = Color(0xFF000000)
private val md_tertiaryContainer = Color(0xFF2a2200)
private val md_onTertiaryContainer = Color(0xFFFFE08A)

private val md_error = Color(0xFFFF6B6B)
private val md_onError = Color(0xFF000000)
private val md_errorContainer = Color(0xFF3a1a1a)
private val md_onErrorContainer = Color(0xFFFFBBBB)

private val md_background = Color(0xFF000000)
private val md_onBackground = Color(0xFFE0E0E0)
private val md_surface = Color(0xFF000000)
private val md_onSurface = Color(0xFFE0E0E0)
private val md_surfaceVariant = Color(0xFF0a0a1a)
private val md_onSurfaceVariant = Color(0xFF8899AA)
private val md_outline = Color(0xFF222244)
private val md_outlineVariant = Color(0xFF161630)

private val MacBridgeDarkColorScheme = darkColorScheme(
    primary = md_primary,
    onPrimary = md_onPrimary,
    primaryContainer = md_primaryContainer,
    onPrimaryContainer = md_onPrimaryContainer,
    secondary = md_secondary,
    onSecondary = md_onSecondary,
    secondaryContainer = md_secondaryContainer,
    onSecondaryContainer = md_onSecondaryContainer,
    tertiary = md_tertiary,
    onTertiary = md_onTertiary,
    tertiaryContainer = md_tertiaryContainer,
    onTertiaryContainer = md_onTertiaryContainer,
    error = md_error,
    onError = md_onError,
    errorContainer = md_errorContainer,
    onErrorContainer = md_onErrorContainer,
    background = md_background,
    onBackground = md_onBackground,
    surface = md_surface,
    onSurface = md_onSurface,
    surfaceVariant = md_surfaceVariant,
    onSurfaceVariant = md_onSurfaceVariant,
    outline = md_outline,
    outlineVariant = md_outlineVariant
)

/**
 * MacBridge theme — always dark, OLED-friendly black background,
 * with a curated indigo-midnight color palette.
 */
@Composable
fun MacBridgeTheme(content: @Composable () -> Unit) {
    val colorScheme = MacBridgeDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Black.toArgb()
            window.navigationBarColor = Color.Black.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
