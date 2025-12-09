package com.example.penjualan_produk_umkm.database.firestore.model

import com.google.firebase.Timestamp

data class Pesanan(
    var id: String = "", // ID String untuk Firestore
    var userId: String = "",
    var totalHarga: Double = 0.0,

    // Kita simpan sebagai String di database agar fleksibel
    var status: String = StatusPesanan.KERANJANG.name,

    // Gunakan Timestamp, bukan LocalDate
    var tanggal: Timestamp = Timestamp.now(),

    var alamat: String = "",

    var ekspedisiId: String? = null,
    var metodePembayaran: MetodePembayaran = MetodePembayaran.CASH
)