package com.example.penjualan_produk_umkm.database.firestore.model

data class ItemPesanan(
    var id: String = "",
    var pesananId: String = "",
    var produkId: String = "",
    var produkNama: String = "",
    var produkHarga: Double = 0.0,
    var jumlah: Int = 0,
    var isSelected: Boolean = false
)
