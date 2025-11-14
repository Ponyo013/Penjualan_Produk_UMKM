package com.example.penjualan_produk_umkm.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.penjualan_produk_umkm.database.Converters

@Entity(tableName = "produk")
@TypeConverters(Converters::class)
data class Produk(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nama")
    val nama: String,

    @ColumnInfo(name = "deskripsi")
    val deskripsi: String,

    @ColumnInfo(name = "spesifikasi")
    val spesifikasi: String,

    @ColumnInfo(name = "harga")
    val harga: Double,

    @ColumnInfo(name = "stok")
    var stok: Int,

    @ColumnInfo(name = "kategori")
    val kategori: String,

    @ColumnInfo(name = "gambar_resource_ids")
    val gambarResourceIds: List<Int>,

    @ColumnInfo(name = "rating")
    val rating: Float = 0f,

    @ColumnInfo(name = "terjual")
    val terjual: Int = 0
)
