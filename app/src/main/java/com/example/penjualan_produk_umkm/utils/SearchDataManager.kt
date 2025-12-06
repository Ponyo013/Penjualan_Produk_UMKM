package com.example.penjualan_produk_umkm.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchDataManager(context: Context) {
    private val prefs = context.getSharedPreferences("dwi_usaha_search_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HISTORY = "KEY_SEARCH_HISTORY"
        private const val KEY_RECENT_VIEWED = "KEY_RECENT_VIEWED"
    }

    // --- PENCARIAN TERAKHIR (String) ---
    fun saveSearchHistory(query: String) {
        if (query.isBlank()) return
        val list = getSearchHistory().toMutableList()
        // Hapus jika sudah ada biar naik ke atas
        list.removeAll { it.equals(query, ignoreCase = true) }
        list.add(0, query)
        if (list.size > 5) list.removeAt(list.size - 1) // Simpan max 5

        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply()
    }

    fun getSearchHistory(): List<String> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearSearchHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    fun removeSearchItem(query: String) {
        val list = getSearchHistory().toMutableList()
        list.remove(query)
        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply()
    }

    // --- TERAKHIR DILIHAT (Product ID) ---
    // Panggil fungsi ini saat User masuk ke Detail Produk
    fun saveRecentlyViewed(productId: String) {
        val list = getRecentlyViewedIds().toMutableList()
        list.remove(productId)
        list.add(0, productId)
        if (list.size > 8) list.removeAt(list.size - 1) // Simpan max 8 produk

        prefs.edit().putString(KEY_RECENT_VIEWED, gson.toJson(list)).apply()
    }

    fun getRecentlyViewedIds(): List<String> {
        val json = prefs.getString(KEY_RECENT_VIEWED, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }
}