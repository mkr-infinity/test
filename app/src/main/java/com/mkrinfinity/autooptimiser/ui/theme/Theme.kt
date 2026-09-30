package com.mkrinfinity.autooptimiser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Ink = Color(0xFF102B35)
private val DeepInk = Color(0xFF071B22)
private val Mint = Color(0xFF306B5E)
private val MintBright = Color(0xFF82C4A8)
private val Copper = Color(0xFFB96843)
private val Sand = Color(0xFFF5F6F1)
private val Paper = Color(0xFFFFFFFF)
private val NightSurface = Color(0xFF122A31)
private val NightSurfaceVariant = Color(0xFF1B3940)
private val Slate = Color(0xFF50656B)
private val Warning = Color(0xFF9B681A)
private val Error = Color(0xFFB64240)

val AutoLightColors = lightColorScheme(
    primary = Mint,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E9E1),
    onPrimaryContainer = Ink,
    secondary = Copper,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5DED2),
    onSecondaryContainer = Color(0xFF4E2113),
    tertiary = Color(0xFF496F86),
    onTertiary = Color.White,
    background = Sand,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE6ECE8),
    onSurfaceVariant = Slate,
    outline = Color(0xFF9AAFA8),
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD7),
    onErrorContainer = Color(0xFF410003)
)

val AutoDarkColors = darkColorScheme(
    primary = MintBright,
    onPrimary = DeepInk,
    primaryContainer = Color(0xFF1E5148),
    onPrimaryContainer = Color(0xFFBDECD3),
    secondary = Color(0xFFE8A17B),
    onSecondary = Color(0xFF411507),
    secondaryContainer = Color(0xFF71351F),
    onSecondaryContainer = Color(0xFFFFDBCC),
    tertiary = Color(0xFFA5CBE2),
    onTertiary = Color(0xFF083546),
    background = DeepInk,
    onBackground = Color(0xFFE2F0EC),
    surface = NightSurface,
    onSurface = Color(0xFFE2F0EC),
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = Color(0xFFB7CAC5),
    outline = Color(0xFF6F8A82),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF680006),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val WarningColor = Warning
val SuccessColor = Mint
val InfoColor = Color(0xFF496F86)
val CopperColor = Copper

@Composable
fun AutoOptimiserTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) AutoDarkColors else AutoLightColors,
        typography = Typography().copy(
            headlineLarge = Typography().headlineLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            titleLarge = Typography().titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            labelLarge = Typography().labelLarge.copy(letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified)
        ),
        content = content
    )
}
