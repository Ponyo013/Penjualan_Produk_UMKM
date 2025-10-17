package com.example.penjualan_produk_umkm

import androidx.compose.runtime.mutableStateListOf
import com.example.penjualan_produk_umkm.model.*
import org.threeten.bp.LocalDate

// Update data dummy
val produkDummyList = mutableStateListOf(
    Produk(1, "Sepeda Gunung", "Sepeda untuk medan berat", 1500000.0, 10, "Sepeda", "https://via.placeholder.com/150?text=Sepeda+Gunung", rating = 4.5f, terjual = 12),
    Produk(2, "Helm Sepeda", "Helm proteksi kepala", 250000.0, 20, "Aksesoris", "https://via.placeholder.com/150?text=Helm+Sepeda", rating = 4.2f, terjual = 30),
    Produk(3, "Sarung Tangan", "Sarung tangan untuk bersepeda", 100000.0, 15, "Aksesoris", "https://via.placeholder.com/150?text=Sarung+Tangan", rating = 4.0f, terjual = 18),
    Produk(4, "Botol Minum", "Botol minum untuk bersepeda", 50000.0, 30, "Aksesoris", "https://via.placeholder.com/150?text=Botol+Minum", rating = 4.7f, terjual = 45),
    Produk(5, "Lampu Sepeda", "Lampu depan sepeda", 120000.0, 25, "Aksesoris", "https://via.placeholder.com/150?text=Lampu+Sepeda", rating = 4.3f, terjual = 22)
)



val dummyUsers = mutableMapOf(
    "user1@example.com" to User(1, "Andi", "user1@example.com", "password123", "customer", "081234567890", "Jl. Melati No. 10, Jakarta"),
    "user2@example.com" to User(2, "Budi", "user2@example.com", "password123", "customer", "081234567891", "Jl. Mawar No. 20, Bandung"),
    "user3@example.com" to User(3, "Citra", "user3@example.com", "password123", "customer", "081234567892", "Jl. Kenanga No. 30, Surabaya")
)

val ekspedisiDummy = mutableStateListOf(
    Ekspedisi(1, "JNE", "jne", 2, 15000.0, "Reguler"),
    Ekspedisi(2, "J&T", "jnt", 1, 20000.0, "Express"),
    Ekspedisi(3, "SiCepat", "sicepat", 3, 12000.0, "Reguler")
)

val dummyItems = mutableStateListOf(
    ItemPesanan(produkDummyList[0], 2),
    ItemPesanan(produkDummyList[1], 1),
    ItemPesanan(produkDummyList[2], 3)
)

val dummyPesanan = mutableStateListOf(
    Pesanan(
        id = 1,
        user = dummyUsers["user1@example.com"]!!,
        items = mutableStateListOf(dummyItems[0], dummyItems[1]),
        status = StatusPesanan.DIPROSES,
        ekspedisi = ekspedisiDummy[0],
        tanggal = LocalDate.of(2025, 10, 16),
        metodePembayaran = MetodePembayaran.TRANSFER_BANK
    ),
    Pesanan(
        id = 2,
        user = dummyUsers["user2@example.com"]!!,
        items = mutableStateListOf(dummyItems[2]),
        status = StatusPesanan.DIKIRIM,
        ekspedisi = ekspedisiDummy[1],
        tanggal = LocalDate.of(2025, 10, 17),
        metodePembayaran = MetodePembayaran.GOPAY
    ),
    Pesanan(
        id = 3,
        user = dummyUsers["user3@example.com"]!!,
        items = mutableStateListOf(dummyItems[1], dummyItems[2]),
        status = StatusPesanan.SELESAI,
        ekspedisi = ekspedisiDummy[2],
        tanggal = LocalDate.of(2025, 10, 17),
        metodePembayaran = MetodePembayaran.OVO
    ),
    Pesanan(
        id = 4,
        user = dummyUsers["user1@example.com"]!!,
        items = mutableStateListOf(dummyItems[0], dummyItems[2]),
        status = StatusPesanan.DIBATALKAN,
        ekspedisi = ekspedisiDummy[0],
        tanggal = LocalDate.of(2025, 10, 13),
        metodePembayaran = MetodePembayaran.CASH
    )
)

val ulasanList = listOf(
    Ulasan(
        id = 1,
        produkId = 1,
        userId = 1,
        rating = 5f,
        komentar = "Kualitas mantap, pengiriman cepat!",
        tanggal = LocalDate.now().minusDays(5)
    ),
    Ulasan(
        id = 2,
        produkId = 1,
        userId = 2,
        rating = 4.5f,
        komentar = "Produk sesuai deskripsi, recommended.",
        tanggal = LocalDate.now().minusDays(3)
    ),
    Ulasan(
        id = 3,
        produkId = 2,
        userId = 3,
        rating = 4f,
        komentar = "Helmnya oke, tapi warna agak beda.",
        tanggal = LocalDate.now().minusDays(7)
    ),
    Ulasan(
        id = 4,
        produkId = 2,
        userId = 2,
        rating = 3.5f,
        komentar = "Cukup nyaman dipakai.",
        tanggal = LocalDate.now().minusDays(2)
    ),
    Ulasan(
        id = 5,
        produkId = 3,
        userId = 1,
        rating = 5f,
        komentar = "Botolnya bagus, tidak bocor.",
        tanggal = LocalDate.now().minusDays(1)
    ),
    Ulasan(
        id = 6,
        produkId = 3,
        userId = 2,
        rating = 4.2f,
        komentar = "Suka banget, kualitas top!",
        tanggal = LocalDate.now()
    )
)