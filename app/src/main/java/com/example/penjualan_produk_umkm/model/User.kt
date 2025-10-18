package com.example.penjualan_produk_umkm.model

import org.threeten.bp.LocalDate

data class User(
    val id: Int,
    val nama: String,
    val email: String,
    var password: String,
    val role: String,
    val noTelepon: String = "",
    var alamat: String = "",
    val tanggal: LocalDate = LocalDate.now(),
)
