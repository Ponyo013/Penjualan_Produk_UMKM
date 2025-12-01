package com.example.penjualan_produk_umkm.database.firestore.model

import com.google.firebase.Timestamp

data class Ulasan(
    var id: String = "",
    var produkId: String = "",
    var userId: String = "",
    var rating: Float = 0f,
    var userName: String? = null,
    var komentar: String = "",
    var tanggal: Timestamp = Timestamp.now()
)
