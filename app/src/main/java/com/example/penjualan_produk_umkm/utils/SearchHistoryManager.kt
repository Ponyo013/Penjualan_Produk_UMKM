package com.example.penjualan_produk_umkm.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryManager(context: Context) {
    private val prefs = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "HISTORY_LIST"

    fun saveHistory(query: String) {
        val list = getHistory().toMutableList()
        if (list.contains(query)) list.remove(query) // Hapus jika duplikat biar naik ke atas
        list.add(0, query) // Tambah di paling atas
        if (list.size > 10) list.removeAt(list.size - 1) // Batasi 10 item

        val json = gson.toJson(list)
        prefs.edit().putString(key, json).apply()
    }

    fun getHistory(): List<String> {
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearHistory() {
        prefs.edit().remove(key).apply()
    }
}