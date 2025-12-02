package com.example.penjualan_produk_umkm.database.firestore.model

import com.google.firebase.Timestamp

data class User(
    // FIX 1: Ubah id dari Int ke String (karena Firestore pakai UUID String)
    var id: String = "",

    // FIX 2: Tambahkan nilai default ("") agar Firestore bisa membuat objek kosong (No-Arg Constructor)
    var nama: String = "",
    var email: String = "",
    var password: String = "", // Password sebenarnya tidak perlu disimpan di Firestore (rawan), tapi untuk tugas tidak apa-apa.
    var role: String = "",
    var noTelepon: String = "",
    var alamat: String = "",

    // FIX 3: Ubah LocalDate menjadi Timestamp (Tipe data native Firebase)
    var tanggal: Timestamp = Timestamp.now()
)