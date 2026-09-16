package com.example.telugucalendar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Orange = Color(0xFFE65100)
private val Cream = Color(0xFFFFF3E0)
private val DarkBrown = Color(0xFF3E2723)

private val LightColors = lightColorScheme(
    primary = Orange,
    secondary = DarkBrown,
    background = Cream,
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Orange,
    secondary = Cream,
    background = Color(0xFF1B1B1B),
    surface = Color(0xFF2A2A2A)
)

@Composable
fun TeluguCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
