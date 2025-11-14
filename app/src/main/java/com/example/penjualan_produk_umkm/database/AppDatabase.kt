package com.example.penjualan_produk_umkm.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.penjualan_produk_umkm.database.dao.*
import com.example.penjualan_produk_umkm.database.model.*

@Database(
    entities = [
        User::class, Produk::class, Ulasan::class, Pesanan::class,
        ItemPesanan::class, Ekspedisi::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun produkDao(): ProdukDao
    abstract fun ulasanDao(): UlasanDao
    abstract fun pesananDao(): PesananDao
    abstract fun itemPesananDao(): ItemPesananDao
    abstract fun ekspedisiDao(): EkspedisiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "umkm_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
