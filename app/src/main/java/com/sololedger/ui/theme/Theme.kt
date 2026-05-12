package com.sololedger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class Aesthetic(
    val surface: Color,
    val foreground: Color,
    val accent: Color,
    val muted: Color,
    val card: Color,
    val surfaceAlt: Color,
    val accentAlt: Color,
    val accentBg: Color,
    val isDark: Boolean,
    val navStyle: Int
)

val AESTHETICS = listOf(
    Aesthetic(Color(0xFFF8F6F1), Color(0xFF1A1A2E), Color(0xFF6C63FF), Color(0xFF9B9B9B), Color(0xFFFFFFFF), Color(0xFFF0EEE8), Color(0xFFEEE8DC), Color(0xFF6C63FF).copy(alpha = 0.15f), false, 0),
    Aesthetic(Color(0xFF1A1A2E), Color(0xFFF8F6F1), Color(0xFFFF6B6B), Color(0xFF888888), Color(0xFF252540), Color(0xFF2D2D4A), Color(0xFF252540), Color(0xFFFF6B6B).copy(alpha = 0.15f), true, 1),
    Aesthetic(Color(0xFFF0F4F8), Color(0xFF1E3A5F), Color(0xFF00BCD4), Color(0xFF90A4AE), Color(0xFFFFFFFF), Color(0xFFE3F2FD), Color(0xFFE8EEF4), Color(0xFF00BCD4).copy(alpha = 0.15f), false, 2),
    Aesthetic(Color(0xFF1A1A2E), Color(0xFFE8D5B7), Color(0xFFFF9B4D), Color(0xFF888888), Color(0xFF252540), Color(0xFF2D2D4A), Color(0xFF252540), Color(0xFFFF9B4D).copy(alpha = 0.15f), true, 3),
    Aesthetic(Color(0xFF0D1117), Color(0xFFE6EDF3), Color(0xFF58A6FF), Color(0xFF8B949E), Color(0xFF161B22), Color(0xFF21262D), Color(0xFF0D1117), Color(0xFF58A6FF).copy(alpha = 0.15f), true, 4),
    Aesthetic(Color(0xFFFAFAFA), Color(0xFF2D2D2D), Color(0xFF4CAF50), Color(0xFF9E9E9E), Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFFFFFFFF), Color(0xFF4CAF50).copy(alpha = 0.15f), false, 5),
    Aesthetic(Color(0xFF0F1923), Color(0xFFF0F6FF), Color(0xFF00E5FF), Color(0xFF4A5568), Color(0xFF1A2530), Color(0xFF243447), Color(0xFF0F1923), Color(0xFF00E5FF).copy(alpha = 0.15f), true, 6),
    Aesthetic(Color(0xFFFDF6E3), Color(0xFF5C4B2A), Color(0xFFD4A843), Color(0xFFA89F8A), Color(0xFFFFF8D6), Color(0xFFFFF3C4), Color(0xFFFAF0D0), Color(0xFFD4A843).copy(alpha = 0.15f), false, 7),
    Aesthetic(Color(0xFF1A0020), Color(0xFFF3E5F5), Color(0xFFE040FB), Color(0xFF9E9E9E), Color(0xFF240030), Color(0xFF300040), Color(0xFF1A0020), Color(0xFFE040FB).copy(alpha = 0.15f), true, 8),
    Aesthetic(Color(0xFFFFF0F5), Color(0xFF4A1942), Color(0xFFE91E63), Color(0xFF9E7A9A), Color(0xFFFFDBE4), Color(0xFFFCE4EC), Color(0xFFFFF0F5), Color(0xFFE91E63).copy(alpha = 0.15f), false, 9)
)

@Composable
fun SoloLedgerTheme(aesthetic: Aesthetic, content: @Composable () -> Unit) {
    val scheme = if (aesthetic.isDark) {
        darkColorScheme(primary = aesthetic.accent, onPrimary = Color.White, surface = aesthetic.surface, onSurface = aesthetic.foreground, surfaceVariant = aesthetic.surfaceAlt, background = aesthetic.surface, onBackground = aesthetic.foreground)
    } else {
        lightColorScheme(primary = aesthetic.accent, onPrimary = Color.White, surface = aesthetic.surface, onSurface = aesthetic.foreground, surfaceVariant = aesthetic.surfaceAlt, background = aesthetic.surface, onBackground = aesthetic.foreground)
    }
    MaterialTheme(colorScheme = scheme, content = content)
}