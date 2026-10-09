package com.example.rc_remort.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = NeonCyanLight,
    onSecondary = Color.Black,
    tertiary = StatusGreen,
    background = TechDarkBackground,
    surface = TechDarkSurface,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = StatusRed,
    onError = Color.White
)

@Composable
fun RCRemortTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}