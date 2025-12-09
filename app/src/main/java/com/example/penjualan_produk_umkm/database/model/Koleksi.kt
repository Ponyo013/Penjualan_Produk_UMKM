package com.example.penjualan_produk_umkm.database.model

data class Koleksi(
    val id: Int,
    val title: String,
    val subtitle: String,
    val imageResId: Int,
    val categoryFilter: String
)