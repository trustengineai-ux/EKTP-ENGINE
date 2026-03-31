package com.verihubs.nfcreader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warna utama - nuansa biru profesional Verihubs
val PrimaryBlue = Color(0xFF1A56DB)
val PrimaryBlueDark = Color(0xFF0F3FA6)
val SecondaryTeal = Color(0xFF0EA5E9)
val BackgroundDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val CardDark = Color(0xFF334155)
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    secondary = SecondaryTeal,
    background = BackgroundDark,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    secondary = SecondaryTeal,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B),
    error = ErrorRed
)

@Composable
fun NFCVerihubsTheme(
    darkTheme: Boolean = true, // default dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
