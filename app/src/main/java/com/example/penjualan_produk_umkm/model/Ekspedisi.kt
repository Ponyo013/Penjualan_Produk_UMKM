package com.example.penjualan_produk_umkm.model

data class Ekspedisi(
    val id: Int,
    val nama: String,
    val kode: String,
    val estimasiHari: Int,
    val biaya: Double,
    val layanan: String? = null,
    var isActive: Boolean = true
)
