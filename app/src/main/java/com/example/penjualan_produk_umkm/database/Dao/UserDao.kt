package com.example.penjualan_produk_umkm.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.penjualan_produk_umkm.database.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM user WHERE id = :userId LIMIT 1")
    fun getUserByIdLive(userId: Int): LiveData<User?>
    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user WHERE no_telepon = :noTelepon LIMIT 1")
    suspend fun getUserByNoTelepon(noTelepon: String): User?

    @Query("SELECT * FROM user WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM user WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Int): User?

    @Query("SELECT * FROM user")
    fun getAllUsers(): LiveData<List<User>>

}
