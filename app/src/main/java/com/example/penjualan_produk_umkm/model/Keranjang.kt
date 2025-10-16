package com.example.penjualan_produk_umkm.model

data class Keranjang(
    val user: User,
    val items: MutableList<ItemPesanan> = mutableListOf()
)