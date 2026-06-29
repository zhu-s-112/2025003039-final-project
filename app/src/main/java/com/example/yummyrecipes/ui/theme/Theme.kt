package com.example.yummyrecipes.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Orange500,
    onPrimary = Color.White,
    primaryContainer = Orange200,
    onPrimaryContainer = OnSurfaceLight,
    secondary = Olive,
    onSecondary = Color.White,
    secondaryContainer = Olive.copy(alpha = 0.15f),
    tertiary = Cinnamon,
    onTertiary = Color.White,
    tertiaryContainer = Cinnamon.copy(alpha = 0.15f),
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Orange200.copy(alpha = 0.35f),
    onSurfaceVariant = OnSurfaceLight.copy(alpha = 0.65f),
    outline = Orange300.copy(alpha = 0.5f),
    outlineVariant = Orange200.copy(alpha = 0.4f),
    error = BerryRed,
    errorContainer = BerryRed.copy(alpha = 0.1f),
    inverseSurface = OnSurfaceLight,
    inverseOnSurface = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Orange400,
    onPrimary = Color.White,
    primaryContainer = Orange500.copy(alpha = 0.25f),
    onPrimaryContainer = Orange200,
    secondary = Color(0xFF9ACD32),
    onSecondary = Color(0xFF1A2E00),
    secondaryContainer = Color(0xFF9ACD32).copy(alpha = 0.15f),
    tertiary = Color(0xFFDEB887),
    onTertiary = Color(0xFF3A2010),
    tertiaryContainer = Color(0xFFDEB887).copy(alpha = 0.15f),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Orange500.copy(alpha = 0.1f),
    onSurfaceVariant = OnSurfaceDark.copy(alpha = 0.6f),
    outline = Orange300.copy(alpha = 0.3f),
    outlineVariant = Orange300.copy(alpha = 0.15f),
    error = Color(0xFFFF6B6B),
    errorContainer = Color(0x55FF6B6B),
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = OnSurfaceLight
)

@Composable
fun YummyRecipesTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
