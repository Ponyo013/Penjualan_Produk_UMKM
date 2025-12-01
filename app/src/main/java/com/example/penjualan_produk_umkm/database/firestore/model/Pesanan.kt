package com.example.penjualan_produk_umkm.database.firestore.model

import com.google.firebase.Timestamp

enum class StatusPesanan {
    DIPROSES,
    DIKIRIM,
    SELESAI,
    DIBATALKAN
}

enum class MetodePembayaran {
    CASH,
    TRANSFER_BANK,
    GOPAY,
    OVO,
    DANA,
}

data class Pesanan(
    var id: String = "",
    var userId: String = "",
    var totalHarga: Double = 0.0,
    var status: StatusPesanan = StatusPesanan.DIPROSES,
    var tanggal: Timestamp = Timestamp.now(),
    var ekspedisiId: String? = null,
    var metodePembayaran: MetodePembayaran = MetodePembayaran.CASH
)
