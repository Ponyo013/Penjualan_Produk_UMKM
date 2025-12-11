package com.example.penjualan_produk_umkm.database

enum class NotificationType {
    PESANAN, UMUM
}

data class Notification(
    var notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val readStatus: Boolean = false,
    val recipient: String? = null,
    val pesananId: String? = null, // Field baru untuk ID pesanan
    val type: String = NotificationType.UMUM.name // Field baru untuk tipe notifikasi
)
