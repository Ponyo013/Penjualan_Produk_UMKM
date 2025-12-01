package com.example.penjualan_produk_umkm.database.firestore.model

data class Produk(
    var id: String = "",
    var nama: String = "",
    var deskripsi: String = "",
    var spesifikasi: String = "",
    var harga: Double = 0.0,
    var stok: Int = 0,
    var kategori: String = "",
    var gambarUrl: String = "",
    var rating: Float = 0f,
    var terjual: Int = 0
)