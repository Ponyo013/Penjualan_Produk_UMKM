package com.example.penjualan_produk_umkm.database.firestore.model

import com.google.firebase.Timestamp

data class Ulasan(
    var id: String = "", // ID dokumen Firestore (String)
    var produkId: String = "",
    var userId: String = "",
    var userName: String? = null, // Tambahan: Simpan nama user agar tidak perlu join table
    var rating: Float = 0f,
    var komentar: String = "",
    var tanggal: Timestamp = Timestamp.now() // Gunakan Timestamp Firebase
)