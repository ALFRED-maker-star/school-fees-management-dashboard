package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = TealPrimary,
  onPrimary = Color.White,
  primaryContainer = TealContainer,
  onPrimaryContainer = OnTealContainer,
  secondary = TealDark,
  onSecondary = Color.White,
  secondaryContainer = TealContainer,
  onSecondaryContainer = OnTealContainer,
  tertiary = PartialAmber,
  onTertiary = Color.White,
  background = SlateBackground,
  onBackground = TextPrimary,
  surface = SlateSurface,
  onSurface = TextPrimary,
  surfaceVariant = SlateSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = SlateBorder,
  outlineVariant = SlateBorderHover,
  error = OwingRed,
  onError = Color.White,
  errorContainer = OwingRedContainer,
  onErrorContainer = OnOwingRed
)

private val DarkColorScheme = darkColorScheme(
  primary = TealLight,
  onPrimary = Color(0xFF042F2E),
  primaryContainer = TealDark,
  onPrimaryContainer = TealContainer,
  secondary = TealLight,
  onSecondary = Color(0xFF042F2E),
  background = Color(0xFF0F172A),
  onBackground = Color(0xFFF8FAFC),
  surface = Color(0xFF1E293B),
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Color(0xFF334155),
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Color(0xFF475569),
  error = OwingRed,
  onError = Color.White
)

@Composable
fun FeeTrackTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = FeeTrackTypography,
    content = content
  )
}
