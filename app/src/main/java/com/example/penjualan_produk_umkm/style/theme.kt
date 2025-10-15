package com.example.penjualan_produk_umkm.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun UMKMTheme(
    content: @Composable () -> Unit
) {
    val colors = LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}