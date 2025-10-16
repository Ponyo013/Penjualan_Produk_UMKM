package com.example.penjualan_produk_umkm

import androidx.compose.runtime.mutableStateListOf
import com.example.penjualan_produk_umkm.model.*
import org.threeten.bp.LocalDate

val produkDummyList = mutableStateListOf(
    Produk(1, "Sepeda Gunung", "Sepeda untuk medan berat", 1500000.0, 10, "Sepeda", ""),
    Produk(2, "Helm Sepeda", "Helm proteksi kepala", 250000.0, 20, "Aksesoris", ""),
    Produk(3, "Sarung Tangan", "Sarung tangan untuk bersepeda", 100000.0, 15, "Aksesoris", ""),
    Produk(4, "Botol Minum", "Botol minum untuk bersepeda", 50000.0, 30, "Aksesoris", ""),
    Produk(5, "Lampu Sepeda", "Lampu depan sepeda", 120000.0, 25, "Aksesoris", "")
)

val dummyUsers = mutableMapOf(
    "user1@example.com" to User(1, "Andi", "user1@example.com", "password123", "customer", "081234567890"),
    "user2@example.com" to User(2, "Budi", "user2@example.com", "password123", "customer", "081234567891"),
    "user3@example.com" to User(3, "Citra", "user3@example.com", "password123", "customer", "081234567892")
)

val ekspedisiDummy = listOf(
    Ekspedisi(1, "JNE", "jne", 2, 15000.0, "Reguler"),
    Ekspedisi(2, "J&T", "jnt", 1, 20000.0, "Express"),
    Ekspedisi(3, "SiCepat", "sicepat", 3, 12000.0, "Reguler")
)

val dummyItems = listOf(
    ItemPesanan(produkDummyList[0], 2),
    ItemPesanan(produkDummyList[1], 1),
    ItemPesanan(produkDummyList[2], 3)
)

val dummyPesanan = listOf(
    Pesanan(
        id = 1,
        user = dummyUsers["user1@example.com"]!!,
        items = listOf(dummyItems[0], dummyItems[1]),
        status = StatusPesanan.DIPROSES,
        ekspedisi = ekspedisiDummy[0], // JNE
        tanggal = LocalDate.of(2025, 10, 14)
    ),
    Pesanan(
        id = 2,
        user = dummyUsers["user2@example.com"]!!,
        items = listOf(dummyItems[2]),
        status = StatusPesanan.DIKIRIM,
        ekspedisi = ekspedisiDummy[1], // J&T
        tanggal = LocalDate.of(2025, 10, 15)
    ),
    Pesanan(
        id = 3,
        user = dummyUsers["user3@example.com"]!!,
        items = listOf(dummyItems[1], dummyItems[2]),
        status = StatusPesanan.SELESAI,
        ekspedisi = ekspedisiDummy[2], // SiCepat
        tanggal = LocalDate.of(2025, 10, 12)
    ),
    Pesanan(
        id = 4,
        user = dummyUsers["user1@example.com"]!!,
        items = listOf(dummyItems[0], dummyItems[2]),
        status = StatusPesanan.DIBATALKAN,
        ekspedisi = ekspedisiDummy[0], // JNE
        tanggal = LocalDate.of(2025, 10, 13)
    )
)