package com.example.penjualan_produk_umkm.model

data class Produk(
    val id: Int,
    val nama: String,
    val deskripsi: String,
    val harga: Double,
    val stok: Int,
    val kategori: String,
    val gambarUrl: String,
    val rating: Float = 0f,
    val terjual: Int = 0
)