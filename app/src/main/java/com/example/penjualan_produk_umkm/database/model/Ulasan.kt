package com.example.penjualan_produk_umkm.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(
    tableName = "ulasan",
    foreignKeys = [
        ForeignKey(
            entity = Produk::class,
            parentColumns = ["id"],
            childColumns = ["produk_id"],
            onDelete = ForeignKey.CASCADE
        ),

        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Ulasan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "produk_id", index = true)
    val produkId: Int,

    @ColumnInfo(name = "user_id", index = true)
    val userId: Int,

    @ColumnInfo(name = "rating")
    val rating: Float,

    @ColumnInfo(name = "komentar")
    val komentar: String,

    @ColumnInfo(name = "tanggal")
    val tanggal: LocalDate
)
