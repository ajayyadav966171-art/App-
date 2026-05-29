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

private val PolishDarkColorScheme = darkColorScheme(
    primary = PolishDarkPrimary,
    secondary = PolishDarkSecondary,
    tertiary = PolishDarkTertiary,
    background = PolishDarkBg,
    surface = PolishDarkSurface,
    onBackground = PolishDarkOnBg,
    onSurface = PolishDarkOnSurface,
    primaryContainer = PolishDarkSecondaryContainer,
    onPrimaryContainer = PolishDarkPrimary,
    secondaryContainer = PolishDarkSurface,
    onSecondaryContainer = PolishDarkPrimary,
    surfaceVariant = Color(0xFF3B383E),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

private val PolishLightColorScheme = lightColorScheme(
    primary = PolishLightPrimary,
    secondary = PolishLightSecondary,
    tertiary = PolishLightTertiary,
    background = PolishLightBg,
    surface = PolishLightSurface,
    onBackground = PolishLightOnBg,
    onSurface = PolishLightOnSurface,
    primaryContainer = PolishLightSecondaryContainer,
    onPrimaryContainer = PolishLightSecondary,
    secondaryContainer = Color(0xFFF3EDF7),
    onSecondaryContainer = PolishLightPrimary,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = PolishLightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force consistent elegant colors by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PolishDarkColorScheme
        else -> PolishLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
