package com.example.penjualan_produk_umkm.database.firestore.model

data class Ekspedisi(
    var id: String = "",

    var nama: String = "",

    val kode: String = "",

    var estimasiHari: Int = 0,

    var biaya: Double = 0.0,

    val layanan: String? = null,
    @field:JvmField
    var isActive: Boolean = true
)
