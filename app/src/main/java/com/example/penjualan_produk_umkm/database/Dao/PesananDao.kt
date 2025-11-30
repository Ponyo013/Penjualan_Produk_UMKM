package com.example.penjualan_produk_umkm.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.penjualan_produk_umkm.database.relation.PesananWithItems
import com.example.penjualan_produk_umkm.database.model.Pesanan
import com.example.penjualan_produk_umkm.database.model.StatusPesanan
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import kotlinx.coroutines.flow.Flow

@Dao
interface PesananDao {
    @Insert
    suspend fun insert(pesanan: Pesanan): Long

    @Update
    suspend fun update(pesanan: Pesanan)

    @Query("SELECT * from pesanan WHERE user_id = :userId")
    fun getPesananForUser(userId: Int): LiveData<List<Pesanan>>

    @Transaction
    @Query("SELECT * FROM pesanan WHERE user_id = :userId AND status = :status ORDER BY id DESC")
    suspend fun getPesananByUserIdAndStatus(userId: Int, status: StatusPesanan): List<PesananWithItems>

    // Ambil semua pesanan milik user
    @Transaction
    @Query("SELECT * FROM pesanan WHERE user_id = :userId")
    suspend fun getPesananWithUserAndEkspedisi(userId: Int): List<PesananWithItems>

    // Ambil semua item + produk untuk 1 pesanan
    @Transaction
    @Query("SELECT * FROM item_pesanan WHERE pesanan_id = :pesananId")
    suspend fun getItemPesananWithProdukByPesananId(pesananId: Int): List<ItemPesananWithProduk>

    // Ambil semua pesanan (untuk owner dashboard)
    @Transaction
    @Query("SELECT * FROM pesanan ORDER BY id DESC")
    fun getAllPesananWithItems(): Flow<List<PesananWithItems>>

    // Ambil pesanan berdasarkan status
    @Transaction
    @Query("SELECT * FROM pesanan WHERE status = :status ORDER BY id DESC")
    fun getPesananByStatus(status: StatusPesanan): Flow<List<PesananWithItems>>

    // Get pending order for a user (used for cart)
    @Query("SELECT * FROM pesanan WHERE user_id = :userId AND status = 'DIPROSES' LIMIT 1")
    suspend fun getPendingPesananForUser(userId: Int): Pesanan?

    // Update status pesanan
    @Query("UPDATE pesanan SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: StatusPesanan)
}
