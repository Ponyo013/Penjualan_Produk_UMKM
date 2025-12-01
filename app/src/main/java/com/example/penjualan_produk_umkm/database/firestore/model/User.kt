package com.example.penjualan_produk_umkm.database.firestore.model

data class User(
    var id: String = "",
    var nama: String = "",
    var email: String = "",
    var role: String = "",
    var noTelepon: String = "",
    var alamat: String = ""
)
