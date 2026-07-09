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
    primary = EmeraldGreenDark,
    secondary = MintGreen,
    tertiary = LuminousGold,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color(0xFF040A06),
    onSecondary = Color(0xFF040A06),
    onTertiary = Color(0xFF040A06),
    onBackground = Color(0xFFE8F5E9),
    onSurface = Color(0xFFE8F5E9),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EmeraldGreenLight,
    secondary = ForestSage,
    tertiary = RichGold,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF1E1E1E),
    onBackground = Color(0xFF152A1E),
    onSurface = Color(0xFF152A1E),
  )

@Composable
fun WealthBuilderTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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
