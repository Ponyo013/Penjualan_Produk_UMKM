package com.example.penjualan_produk_umkm.model

data class ItemPesanan(
    val produk: Produk,
    val jumlah: Int,
    val subtotal: Double = produk.harga * jumlah
)
