package com.example.penjualan_produk_umkm.database

data class Notification(
    var id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val readStatus: Boolean = false,
    val recipient: String? = null
)
