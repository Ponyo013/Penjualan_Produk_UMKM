package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.dao.ProdukDao
import com.example.penjualan_produk_umkm.database.model.Produk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProdukViewModel(private val produkDao: ProdukDao) : ViewModel() {

    // Ambil semua produk dari database
    val allProduk: LiveData<List<Produk>> = produkDao.getAllProduk()

    // Tambah produk
    fun insertProduk(produk: Produk) {
        viewModelScope.launch(Dispatchers.IO) {
            produkDao.insert(produk)
        }
    }

    fun getProdukById(id: Int): LiveData<Produk> = produkDao.getProduk(id)

    // Update produk (misal ubah stok)
    fun updateProduk(produk: Produk) {
        viewModelScope.launch(Dispatchers.IO) {
            produkDao.update(produk)
        }
    }

    // Hapus produk
    fun delete(produk: Produk) {
        viewModelScope.launch(Dispatchers.IO) {
            produkDao.delete(produk)
        }
    }
}
