package com.example.penjualan_produk_umkm.database.firestore.model

data class Ekspedisi(
    var id: String = "",
    var nama: String = "",
    var kode: String = "",
    var estimasiHari: Int = 0,
    var biaya: Double = 0.0,
    var layanan: String? = null,
    var isActive: Boolean = true
)
