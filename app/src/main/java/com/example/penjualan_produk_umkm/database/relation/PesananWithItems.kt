package com.example.penjualan_produk_umkm.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.penjualan_produk_umkm.database.model.Ekspedisi
import com.example.penjualan_produk_umkm.database.model.Pesanan
import com.example.penjualan_produk_umkm.database.model.User

data class PesananWithItems(
    @Embedded
    val pesanan: Pesanan,

    @Relation(
        parentColumn = "ekspedisi_id",
        entityColumn = "id"
    )
    val ekspedisi: Ekspedisi? = null,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "id"
    )
    val user: User? = null,

)
