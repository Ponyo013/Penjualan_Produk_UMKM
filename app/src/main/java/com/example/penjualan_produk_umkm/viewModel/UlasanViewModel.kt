package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.dao.UlasanDao
import com.example.penjualan_produk_umkm.database.model.Ulasan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UlasanViewModel(private val ulasanDao: UlasanDao) : ViewModel() {

    // Ambil ulasan untuk produk tertentu
    fun getUlasanByProdukId(produkId: Int): LiveData<List<Ulasan>> {
        return ulasanDao.getUlasanByProdukId(produkId)
    }

    // Insert ulasan baru
    fun insertUlasan(ulasan: Ulasan) {
        viewModelScope.launch(Dispatchers.IO) {
            ulasanDao.insertUlasan(ulasan)
        }
    }
}
