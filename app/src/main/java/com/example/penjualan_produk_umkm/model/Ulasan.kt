package com.example.penjualan_produk_umkm.model

import org.threeten.bp.LocalDate

data class Ulasan(
    val id: Int,
    val produkId: Int,
    val userId: Int,
    val rating: Float,
    val komentar: String,
    val tanggal: LocalDate = LocalDate.now()
)