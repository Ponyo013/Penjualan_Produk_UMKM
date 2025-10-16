package com.example.penjualan_produk_umkm.model

data class ItemPesanan(
    val produk: Produk,
    var jumlah: Int,
) {
    val subtotal: Double
        get() = produk.harga * jumlah
}
