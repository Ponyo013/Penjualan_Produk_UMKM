package com.example.penjualan_produk_umkm.model

import org.threeten.bp.LocalDate
enum class StatusPesanan {
    DIPROSES,
    DIKIRIM,
    SELESAI,
    DIBATALKAN
}

data class Pesanan (
    val id: Int,
    val user: User,
    val items: List<ItemPesanan>,
    val totalHarga: Double = items.sumOf { it.subtotal },
    val status: StatusPesanan = StatusPesanan.DIPROSES,
    val tanggal: LocalDate = LocalDate.now(),
    val ekspedisi: Ekspedisi? = null
)
