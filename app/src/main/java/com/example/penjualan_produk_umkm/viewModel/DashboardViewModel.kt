
package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.penjualan_produk_umkm.database.dao.PesananDao
import com.example.penjualan_produk_umkm.database.relation.PesananWithItems

class DashboardViewModel(private val pesananDao: PesananDao) : ViewModel() {

    val allPesanan: LiveData<List<PesananWithItems>> = pesananDao.getAllPesananWithItems().asLiveData()
}
