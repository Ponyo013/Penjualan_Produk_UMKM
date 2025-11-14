package com.example.penjualan_produk_umkm.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.penjualan_produk_umkm.database.model.Ulasan

@Dao
interface UlasanDao {
    @Query("SELECT * FROM ulasan WHERE produk_id = :produkId ORDER BY tanggal DESC")
    fun getUlasanByProdukId(produkId: Int): LiveData<List<Ulasan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUlasan(ulasan: Ulasan)
}
