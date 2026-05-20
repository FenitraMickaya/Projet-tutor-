package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = BlueAccent,
    background = BackgroundDark,
    surface = BackgroundDark,
    onPrimary = Color.White,
    onBackground = TextLight,
    onSurface = TextLight,
    secondary = CardBackgroundDark,
    onSecondary = TextLight,
    surfaceVariant = StatsBackgroundDark,
    outline = OutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BlueAccent,
    background = BackgroundLight,
    surface = BackgroundLight,
    onPrimary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    secondary = CardBackground,
    onSecondary = CardTextOnColor,
    surfaceVariant = StatsBackground,
    outline = OutlineBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Custom theme control
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
