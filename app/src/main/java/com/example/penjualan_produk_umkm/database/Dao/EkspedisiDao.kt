package com.example.penjualan_produk_umkm.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.penjualan_produk_umkm.database.model.Ekspedisi

@Dao
interface EkspedisiDao {

    @Query("SELECT * FROM ekspedisi")
    suspend fun getAll(): List<Ekspedisi>

    @Query("SELECT * FROM ekspedisi WHERE is_active = 1")
    fun getActive(): LiveData<List<Ekspedisi>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ekspedisi: Ekspedisi)

    @Update
    suspend fun update(ekspedisi: Ekspedisi)

    @Delete
    suspend fun delete(ekspedisi: Ekspedisi)
}
