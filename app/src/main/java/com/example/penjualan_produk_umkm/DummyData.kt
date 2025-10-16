package com.example.penjualan_produk_umkm

import androidx.compose.runtime.mutableStateListOf
import com.example.penjualan_produk_umkm.model.Keranjang
import com.example.penjualan_produk_umkm.model.Pesanan
import com.example.penjualan_produk_umkm.model.Produk
import com.example.penjualan_produk_umkm.model.User

val produkDummyList = mutableStateListOf<Produk>()
val dummyUsers = mutableMapOf<String, User>()

val dummyKeranjang = mutableMapOf<User, Keranjang>()

val dummyPesanan = mutableStateListOf<Pesanan>()