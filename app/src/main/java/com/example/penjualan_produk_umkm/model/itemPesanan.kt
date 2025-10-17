package com.example.penjualan_produk_umkm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemPesanan(
    val produk: Produk,
    var jumlah: Int,
    var isSelected: Boolean = true
) : Parcelable {
    val subtotal: Double
        get() = produk.harga * jumlah
}
