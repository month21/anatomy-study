package com.month21.anatomy.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary          = Color(0xFF111827),
    onPrimary        = Color.White,
    background       = Color(0xFFF8FAFC),
    surface          = Color.White,
    onBackground     = Color(0xFF111827),
    onSurface        = Color(0xFF111827),
    surfaceVariant   = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280),
    outline          = Color(0xFFE5E7EB),
)

@Composable
fun AnatomyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
