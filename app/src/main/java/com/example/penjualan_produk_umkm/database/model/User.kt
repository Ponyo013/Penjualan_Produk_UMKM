package com.example.penjualan_produk_umkm.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nama")
    val nama: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password")
    var password: String,

    @ColumnInfo(name = "role")
    val role: String,

    @ColumnInfo(name = "no_telepon")
    val noTelepon: String = "",

    @ColumnInfo(name = "alamat")
    var alamat: String = "",

    @ColumnInfo(name = "tanggal_dibuat")
    val tanggal: LocalDate = LocalDate.now(),
)
