package com.example.penjualan_produk_umkm.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import com.example.penjualan_produk_umkm.database.model.ItemPesanan

@Dao
interface ItemPesananDao {
    @Insert
    fun insertAll(items: List<ItemPesanan>)

    @Insert
    fun insert(item: ItemPesanan)

    @Update
    fun update(item: ItemPesanan)

    @Delete
    fun delete(item: ItemPesanan)

    @Transaction
    @Query("SELECT * FROM item_pesanan")
    fun getAllItemsWithProduk(): LiveData<List<ItemPesananWithProduk>>

    // Versi suspend (untuk ViewModel atau logic async)
    @Transaction
    @Query("SELECT * FROM item_pesanan WHERE pesanan_id = :pesananId")
    suspend fun getItemsWithProdukByPesananId(pesananId: Int): List<ItemPesananWithProduk>

    // Versi LiveData (kalau mau reactive di UI Compose)
    @Transaction
    @Query("SELECT * FROM item_pesanan WHERE pesanan_id = :pesananId")
    fun observeItemsWithProdukByPesananId(pesananId: Int): LiveData<List<ItemPesananWithProduk>>
}
