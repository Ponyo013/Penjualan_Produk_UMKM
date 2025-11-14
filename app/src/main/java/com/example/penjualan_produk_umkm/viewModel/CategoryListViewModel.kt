package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.*
import com.example.penjualan_produk_umkm.database.dao.ProdukDao
import com.example.penjualan_produk_umkm.database.model.Produk

class CategoryListViewModel(
    private val produkDao: ProdukDao,
    private val categoryName: String?
) : ViewModel() {

    private val _allProduk = MutableLiveData<List<Produk>>()
    val allProduk: LiveData<List<Produk>> = _allProduk

    init {
        loadProduk()
    }

    private fun loadProduk() {
        produkDao.getAllProduk().observeForever { produkList ->
            _allProduk.value = produkList.filter {
                categoryName == null || it.kategori.equals(categoryName, ignoreCase = true)
            }
        }
    }
}
