package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DualSyncDarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ElectricBlueDark,
    onPrimaryContainer = Color.White,
    secondary = NeonViolet,
    onSecondary = Color.White,
    secondaryContainer = NeonVioletDark,
    onSecondaryContainer = Color.White,
    tertiary = StatusCyan,
    onTertiary = Color.Black,
    background = ObsidianBlack,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF374151),
    error = StatusRose,
    onError = Color.White
)

val DualSyncLightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ElectricBlueLight,
    onPrimaryContainer = Color.Black,
    secondary = NeonViolet,
    onSecondary = Color.White,
    secondaryContainer = NeonVioletLight,
    onSecondaryContainer = Color.Black,
    tertiary = StatusCyan,
    onTertiary = Color.White,
    background = CanvasWhite,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1),
    error = StatusRose,
    onError = Color.White
)

@Composable
fun DualSyncTheme(
    darkTheme: Boolean = true, // Default to dark theme as per specifications
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DualSyncDarkColorScheme else DualSyncLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
