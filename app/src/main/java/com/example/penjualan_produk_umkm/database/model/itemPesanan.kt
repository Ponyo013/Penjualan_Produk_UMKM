package com.example.penjualan_produk_umkm.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "item_pesanan",
    foreignKeys = [
        ForeignKey(
            entity = Pesanan::class,
            parentColumns = ["id"],
            childColumns = ["pesanan_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Produk::class,
            parentColumns = ["id"],
            childColumns = ["produk_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ItemPesanan(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "pesanan_id", index = true)
    var pesananId: Int,

    @ColumnInfo(name = "produk_id", index = true)
    val produkId: Int,

    @ColumnInfo(name = "jumlah")
    var jumlah: Int,

    var isSelected: Boolean = false
) : Parcelable
