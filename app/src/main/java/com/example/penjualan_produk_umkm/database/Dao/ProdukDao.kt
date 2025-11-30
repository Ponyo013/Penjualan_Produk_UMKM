package com.example.penjualan_produk_umkm.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.penjualan_produk_umkm.database.model.Produk

@Dao
interface ProdukDao {
    @Insert
    suspend fun insert(produk: Produk): Long

    @Update
    suspend fun update(produk: Produk)

    @Delete
    suspend fun delete(produk: Produk)

    @Query("SELECT * FROM produk WHERE id = :produkId")
    suspend fun getProdukById(produkId: Int): Produk?

    @Query("SELECT * from produk WHERE id = :produkId")
    fun getProduk(produkId: Int): LiveData<Produk>

    @Query("SELECT * FROM produk ORDER BY id DESC")
    fun getAllProduk(): LiveData<List<Produk>>

    @Query("SELECT * FROM produk ORDER BY id DESC")
    suspend fun getAllProdukOnce(): List<Produk>
}
