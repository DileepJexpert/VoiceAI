package com.voiceai.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleLight,
    onPrimaryContainer = PurpleDark,
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = CoralLight,
    onSecondaryContainer = CoralDark,
    tertiary = Teal,
    onTertiary = Color.White,
    tertiaryContainer = TealLight,
    onTertiaryContainer = TealDark,
    background = Gray50,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray400,
    outlineVariant = Gray200,
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFCD8DF),
    onErrorContainer = Color(0xFF8C001A)
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleLight,
    onPrimary = PurpleDark,
    primaryContainer = Purple,
    onPrimaryContainer = Color.White,
    secondary = CoralLight,
    onSecondary = CoralDark,
    secondaryContainer = Coral,
    onSecondaryContainer = Color.White,
    tertiary = TealLight,
    onTertiary = TealDark,
    tertiaryContainer = Teal,
    onTertiaryContainer = Color.White,
    background = Gray900,
    onBackground = Gray50,
    surface = Color(0xFF1C1B1F),
    onSurface = Gray100,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    outline = Gray600,
    outlineVariant = Gray700,
    error = Color(0xFFCF6679),
    onError = Color(0xFF690012),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFCD8DF)
)

@Composable
fun VoiceAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
