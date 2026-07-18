package com.sleeplessdog.banquerito.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

object BanqueritoColors {
    val Primary = Color(0xFF444F8E)
    val OnPrimary = Color(0xFFFFF9E7)
    val PrimaryContainer = Color(0xFF444F8E)
    val OnPrimaryContainer = Color(0xFFFFF9E7)

    val Background = Color(0xFF131313)
    val OnBackground = Color(0xFFFFF9E7)

    val Surface = Color(0xFF1A1A1A)
    val OnSurface = Color(0xFFFFF9E7)
    val SurfaceVariant = Color(0xFF444F8E)
    val OnSurfaceVariant = Color(0xFFFFF9E7)

    val Error = Color(0xFF85683C)
    val OnError = Color(0xFF853C3C)

    val Success = Color(0xFF268258)
}

val BanqueritoColorScheme: ColorScheme = darkColorScheme(
    primary = BanqueritoColors.Primary,
    onPrimary = BanqueritoColors.OnPrimary,
    primaryContainer = BanqueritoColors.PrimaryContainer,
    onPrimaryContainer = BanqueritoColors.OnPrimaryContainer,
    background = BanqueritoColors.Background,
    onBackground = BanqueritoColors.OnBackground,
    surface = BanqueritoColors.Surface,
    onSurface = BanqueritoColors.OnSurface,
    surfaceVariant = BanqueritoColors.SurfaceVariant,
    onSurfaceVariant = BanqueritoColors.OnSurfaceVariant,
    error = BanqueritoColors.Error,
    onError = BanqueritoColors.OnError,
)