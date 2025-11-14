package com.example.penjualan_produk_umkm.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.penjualan_produk_umkm.database.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.model.Produk

data class ItemPesananWithProduk(
    @Embedded val itemPesanan: ItemPesanan,

    @Relation(
        entity = Produk::class,
        parentColumn = "produk_id",
        entityColumn = "id"
    )
    val produk: Produk,
)