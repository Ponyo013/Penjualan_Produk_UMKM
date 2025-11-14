package com.example.penjualan_produk_umkm.database

import androidx.room.TypeConverter
import com.example.penjualan_produk_umkm.database.model.MetodePembayaran
import com.example.penjualan_produk_umkm.database.model.StatusPesanan
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDate

class Converters {
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return Gson().fromJson(value, object : TypeToken<List<Int>?>() {}.type)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromStatusPesanan(value: String?): StatusPesanan? {
        return value?.let { StatusPesanan.valueOf(it) }
    }

    @TypeConverter
    fun toStatusPesanan(status: StatusPesanan?): String? {
        return status?.name
    }

    @TypeConverter
    fun fromMetodePembayaran(value: String?): MetodePembayaran? {
        return value?.let { MetodePembayaran.valueOf(it) }
    }

    @TypeConverter
    fun toMetodePembayaran(metode: MetodePembayaran?): String? {
        return metode?.name
    }
}
