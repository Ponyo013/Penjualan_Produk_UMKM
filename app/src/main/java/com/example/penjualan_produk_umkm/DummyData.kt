package com.example.penjualan_produk_umkm

import androidx.compose.runtime.mutableStateListOf
import com.example.penjualan_produk_umkm.model.Ekspedisi
import com.example.penjualan_produk_umkm.model.Keranjang
import com.example.penjualan_produk_umkm.model.Pesanan
import com.example.penjualan_produk_umkm.model.Produk
import com.example.penjualan_produk_umkm.model.User

val produkDummyList = mutableStateListOf<Produk>()
val dummyUsers = mutableMapOf<String, User>()

val dummyKeranjang = mutableMapOf<User, Keranjang>()

val dummyPesanan = mutableStateListOf<Pesanan>()

val ekspedisiDummy = listOf(
    Ekspedisi(1, "JNE", "jne", 2, 15000.0, "Reguler"),
    Ekspedisi(2, "J&T", "jnt", 1, 20000.0, "Express"),
    Ekspedisi(3, "SiCepat", "sicepat", 3, 12000.0, "Reguler")
)