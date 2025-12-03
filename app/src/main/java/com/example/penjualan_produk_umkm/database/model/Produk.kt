package com.example.penjualan_produk_umkm.database.firestore.model

data class Produk(
    var id: String = "",

    val nama: String = "",
    val deskripsi: String = "",
    val spesifikasi: String = "",
    val harga: Double = 0.0,
    val stok: Int = 0,
    val kategori: String = "",
    var gambarUrl: String = "",         // URL publik untuk UI
    var imageKitFileId: String = "",    // FileId untuk delete
    val rating: Float = 0f,
    val terjual: Int = 0
)
