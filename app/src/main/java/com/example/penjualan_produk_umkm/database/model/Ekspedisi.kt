package com.example.penjualan_produk_umkm.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ekspedisi")
data class Ekspedisi(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nama")
    val nama: String,

    @ColumnInfo(name = "kode")
    val kode: String,

    @ColumnInfo(name = "estimasi_hari")
    val estimasiHari: Int,

    @ColumnInfo(name = "biaya")
    val biaya: Double,

    @ColumnInfo(name = "layanan")
    val layanan: String? = null,

    @ColumnInfo(name = "is_active")
    var isActive: Boolean = true
)
