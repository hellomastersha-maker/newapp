package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = JarvisPrimaryCyan,
    secondary = JarvisAccentPurple,
    background = JarvisBackground,
    surface = JarvisCardBg,
    onPrimary = JarvisBackground,
    onSecondary = JarvisTextHigh,
    onBackground = JarvisTextHigh,
    onSurface = JarvisTextHigh
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
