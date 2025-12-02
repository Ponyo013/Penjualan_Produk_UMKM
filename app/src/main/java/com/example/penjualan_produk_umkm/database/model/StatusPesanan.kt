package com.example.penjualan_produk_umkm.database.firestore.model

enum class StatusPesanan {
    KERANJANG, // Status awal saat masuk cart
    DIPROSES,  // Setelah checkout
    DIKIRIM,
    SELESAI,
    DIBATALKAN
}