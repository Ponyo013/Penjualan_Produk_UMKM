package com.example.penjualan_produk_umkm.model


import java.io.Serializable // Import antarmuka Serializable

data class Produk(
    val id: Int,
    val nama: String,
    val deskripsi: String,
    val spesifikasi: String,
    val harga: Double,
    val stok: Int,
    val kategori: String,
    val gambarResourceIds: List<Int>,
    val rating: Float = 0f,
    val terjual: Int = 0
) : Serializable