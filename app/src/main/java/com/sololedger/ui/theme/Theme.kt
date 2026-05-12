package com.sololedger.ui.theme

import androidx.compose.ui.graphics.Color

object Palettes {
    val BlossomPink = Aesthetic("Blossom Pink", Color(0xFFE8537A), Color(0xFFFCE4EC), Color(0xFFFFDBE4), Color(0xFF8B2F50), Color(0xFFFFF0F5), Color(0xFF6D1B36), Color(0xFFFFFFFF.toLong()), true, 0)
    val OceanBlue   = Aesthetic("Ocean Blue",   Color(0xFF2196F3), Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFF0D47A1), Color(0xFFF5FAFF), Color(0xFF0A3062), Color(0xFFFFFFFF.toLong()), true, 1)
    val Sunset      = Aesthetic("Sunset",        Color(0xFFFF9100), Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFE65100), Color(0xFFFFFAF5), Color(0xFFB33C00), Color(0xFFFFFFFF.toLong()), true, 2)
    val ForestGreen = Aesthetic("Forest Green", Color(0xFF4CAF50), Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFF1B5E20), Color(0xFFF5FFF5), Color(0xFF0D3B10), Color(0xFFFFFFFF.toLong()), true, 3)
    val Midnight    = Aesthetic("Midnight",      Color(0xFF7986CB), Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFFCFD8DC), Color(0xFF0F0F1A), Color(0xFF8C9EAD), Color(0xFF242438), true, 4)
    val Aesthetic   = Aesthetic
}

data class Aesthetic(
    val name: String,
    val accent: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onAccent: Color,
    val background: Color,
    val text: Color,
    val card: Color,
    val isDark: Boolean,
    val navStyle: Int
)

val LightPalette = Palettes.BlossomPink
val DarkPalette  = Aesthetic("Midnight", Color(0xFF7986CB), Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFFCFD8DC), Color(0xFF0F0F1A), Color(0xFF8C9EAD), Color(0xFF242438), true, 3)