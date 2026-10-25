package io.github.wcwac.beecount.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF176B57), onPrimary = Color.White,
    secondary = Color(0xFF52665D), tertiary = Color(0xFF3C6474),
    background = Color(0xFFF7F9F7), surface = Color(0xFFF7F9F7),
    surfaceVariant = Color(0xFFDDE5DF), onSurface = Color(0xFF17201C),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF90D5B9), onPrimary = Color(0xFF003829),
    secondary = Color(0xFFBACCC1), tertiary = Color(0xFFA4CDDF),
    background = Color(0xFF101512), surface = Color(0xFF101512),
    surfaceVariant = Color(0xFF3E4942), onSurface = Color(0xFFE0E6DF),
)

@Composable
fun NativeTheme(theme: String = "system", content: @Composable () -> Unit) {
    val dark = when (theme) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
