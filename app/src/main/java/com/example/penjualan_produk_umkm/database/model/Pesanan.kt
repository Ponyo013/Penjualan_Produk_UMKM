package com.example.penjualan_produk_umkm.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.penjualan_produk_umkm.database.Converters
import org.threeten.bp.LocalDate

enum class StatusPesanan {
    DIPROSES,
    DIKIRIM,
    SELESAI,
    DIBATALKAN
}

enum class MetodePembayaran {
    CASH,
    TRANSFER_BANK,
    GOPAY,
    OVO,
    DANA,
}

@Entity(
    tableName = "pesanan",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ekspedisi::class,
            parentColumns = ["id"],
            childColumns = ["ekspedisi_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
@TypeConverters(Converters::class)
data class Pesanan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "user_id", index = true)
    val userId: Int,

    @ColumnInfo(name = "total_harga")
    val totalHarga: Double,

    @ColumnInfo(name = "status")
    val status: StatusPesanan = StatusPesanan.DIPROSES,

    @ColumnInfo(name = "tanggal")
    val tanggal: LocalDate = LocalDate.now(),

    @ColumnInfo(name = "ekspedisi_id", index = true)
    val ekspedisiId: Int? = null,

    @ColumnInfo(name = "metode_pembayaran")
    val metodePembayaran: MetodePembayaran = MetodePembayaran.CASH
)
