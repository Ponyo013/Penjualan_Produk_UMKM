package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.dao.ItemPesananDao
import com.example.penjualan_produk_umkm.database.dao.PesananDao
import com.example.penjualan_produk_umkm.database.model.StatusPesanan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OwnerPesananViewModel(private val pesananDao: PesananDao, private val itemPesananDao: ItemPesananDao) : ViewModel() {

    private val _pesananList = MutableStateFlow<List<PesananWithItems>>(emptyList())
    val pesananList: StateFlow<List<PesananWithItems>> get() = _pesananList

    // Ambil semua pesanan
    fun loadAll() {
        viewModelScope.launch {
            pesananDao.getAllPesananWithItems().collectLatest {
                _pesananList.value = it
            }
        }
    }

    fun getItemsForPesanan(pesananId: Int): LiveData<List<ItemPesananWithProduk>> {
        return itemPesananDao.observeItemsWithProdukByPesananId(pesananId)
    }

    fun getAllItems(): LiveData<List<ItemPesananWithProduk>> {
        return itemPesananDao.getAllItemsWithProduk()
    }

    // Ambil pesanan berdasarkan status
    fun loadByStatus(status: StatusPesanan) {
        viewModelScope.launch {
            pesananDao.getPesananByStatus(status).collectLatest {
                _pesananList.value = it
            }
        }
    }

    // Ubah status pesanan
    fun updateStatus(id: Int, newStatus: StatusPesanan) {
        viewModelScope.launch {
            pesananDao.updateStatus(id, newStatus)
        }
    }
}
