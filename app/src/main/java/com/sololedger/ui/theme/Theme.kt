package com.sololedger.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val SoloInk = Color(0xFF090C14)
val SoloSurface = Color(0xFF121827)
val SoloSurfaceAlt = Color(0xFF1A2235)
val SoloLine = Color(0xFF273148)
val SoloText = Color(0xFFF5F7FB)
val SoloMuted = Color(0xFF9CA4BB)
val SoloDanger = Color(0xFFFF677D)
val SoloSuccess = Color(0xFF52D2A8)

private val soloShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun SoloTheme(accent: Color, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = accent,
            onPrimary = Color.White,
            secondary = Color(0xFF8ED5FF),
            tertiary = Color(0xFFFFC56B),
            background = SoloInk,
            surface = SoloSurface,
            surfaceVariant = SoloSurfaceAlt,
            onBackground = SoloText,
            onSurface = SoloText,
            onSurfaceVariant = SoloMuted,
            outline = SoloLine,
            error = SoloDanger,
        ),
        shapes = soloShapes,
        content = content,
    )
}
