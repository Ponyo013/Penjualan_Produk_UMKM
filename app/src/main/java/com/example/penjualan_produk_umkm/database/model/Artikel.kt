package com.example.penjualan_produk_umkm.database.model

data class Artikel(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val url: String // Link ke web artikel asli
)