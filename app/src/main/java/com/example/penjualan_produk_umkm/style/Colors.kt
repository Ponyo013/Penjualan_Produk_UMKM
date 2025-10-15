package com.example.penjualan_produk_umkm.style

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Grey = Color(0xFFEFEFF0)

val Secondary1 = Color(0xFF1E2E4F)
val Secondary2 = Color(0xFF3B507D)
val Secondary3 = Color(0xFF031C38)

val TextPrimary = Color(0xFF000000)
val TextSecondary = Color(0xFF8C8C8C)
val TextSecondaryDark = Color(0xFF4A4A4A)
val TextDescription = Color(0xFF424242)

// Material color schemes
val LightColors = lightColorScheme(
    primary = Secondary2,
    onPrimary = White,

    secondary = Secondary3,
    onSecondary = White,

    background = White,
    onBackground = Black,

    surface = White,
    onSurface = Black,

    primaryContainer = Grey,
    onPrimaryContainer = Black,

    error = Color(0xFFB00020),
    onError = White,
)