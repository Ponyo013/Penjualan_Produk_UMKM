package com.example.penjualan_produk_umkm.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // userId sekarang String karena Firebase UID
    fun saveUser(id: String, email: String, role: String) {
        prefs.edit().apply {
            putString("user_id", id)
            putString("user_email", email)
            putString("user_role", role)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun getUserId(): String? = prefs.getString("user_id", null)
    fun getUserEmail(): String? = prefs.getString("user_email", null)
    fun getUserRole(): String? = prefs.getString("user_role", "user")
    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun clear() {
        prefs.edit { clear() }
    }
}
