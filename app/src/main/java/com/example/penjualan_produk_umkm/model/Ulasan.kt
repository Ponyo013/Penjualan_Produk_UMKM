// File: com/example/penjualan_produk_umkm/model/Ulasan.kt

package com.example.penjualan_produk_umkm.model

import java.io.Serializable
import org.threeten.bp.LocalDate

data class Ulasan(
    val id: Int,      // <-- HARUS INT
    val produkId: Int, // <-- HARUS INT
    val userId: Int,
    val rating: Float,
    val komentar: String,
    val tanggal: LocalDate
) : Serializable