package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
  primary = BluePrimary,
  onPrimary = BlueOnPrimary,
  primaryContainer = BluePrimaryContainer,
  onPrimaryContainer = BlueOnPrimaryContainer,
  secondary = Slate800,
  onSecondary = Color.White,
  secondaryContainer = Slate100,
  onSecondaryContainer = Slate900,
  tertiary = PurpleRoutine,
  onTertiary = Color.White,
  tertiaryContainer = PurpleRoutineContainer,
  onTertiaryContainer = Slate900,
  background = Slate50,
  onBackground = Slate900,
  surface = Color.White,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate600,
  outline = Slate200,
  outlineVariant = Slate300
)

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF60A5FA),
  onPrimary = Slate950,
  primaryContainer = Color(0xFF1E3A8A),
  onPrimaryContainer = Color(0xFFDBEAFE),
  secondary = Slate200,
  onSecondary = Slate900,
  secondaryContainer = Slate800,
  onSecondaryContainer = Slate100,
  tertiary = Color(0xFFA78BFA),
  onTertiary = Slate950,
  tertiaryContainer = Color(0xFF4C1D95),
  onTertiaryContainer = Color(0xFFEDE9FE),
  background = Slate950,
  onBackground = Slate100,
  surface = Slate900,
  onSurface = Slate100,
  surfaceVariant = Slate800,
  onSurfaceVariant = Slate400,
  outline = Slate700,
  outlineVariant = Slate600
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
