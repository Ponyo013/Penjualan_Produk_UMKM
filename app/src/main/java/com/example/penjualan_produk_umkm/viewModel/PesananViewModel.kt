package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.*
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.dao.ItemPesananDao
import com.example.penjualan_produk_umkm.database.relation.PesananWithItems
import com.example.penjualan_produk_umkm.database.model.StatusPesanan
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import kotlinx.coroutines.launch


// ViewModel untuk menampilkan daftar pesanan user berdasarkan status
class PesananViewModel(private val db: AppDatabase, private val userId: Int, private val itemPesananDao: ItemPesananDao) : ViewModel() {

    private val _pesananDiproses = MutableLiveData<List<PesananWithItems>>()
    val pesananDiproses: LiveData<List<PesananWithItems>> get() = _pesananDiproses

    private val _pesananDikirim = MutableLiveData<List<PesananWithItems>>()
    val pesananDikirim: LiveData<List<PesananWithItems>> get() = _pesananDikirim

    private val _pesananSelesai = MutableLiveData<List<PesananWithItems>>()
    val pesananSelesai: LiveData<List<PesananWithItems>> get() = _pesananSelesai

    private val _pesananDibatalkan = MutableLiveData<List<PesananWithItems>>()
    val pesananDibatalkan: LiveData<List<PesananWithItems>> get() = _pesananDibatalkan

    init {
        loadAllPesanan()
    }

    fun getItemsForPesanan(pesananId: Int): LiveData<List<ItemPesananWithProduk>> {
        return liveData {
            emit(itemPesananDao.getItemsWithProdukByPesananId(pesananId))
        }
    }
    // Load semua pesanan berdasarkan status
    fun loadAllPesanan() {
        viewModelScope.launch {
            _pesananDiproses.value =
                db.pesananDao().getPesananByUserIdAndStatus(userId, StatusPesanan.DIPROSES)
            _pesananDikirim.value =
                db.pesananDao().getPesananByUserIdAndStatus(userId, StatusPesanan.DIKIRIM)
            _pesananSelesai.value =
                db.pesananDao().getPesananByUserIdAndStatus(userId, StatusPesanan.SELESAI)
            _pesananDibatalkan.value =
                db.pesananDao().getPesananByUserIdAndStatus(userId, StatusPesanan.DIBATALKAN)
        }
    }


    // Refresh pesanan berdasarkan status tertentu
    fun refreshPesanan(status: StatusPesanan) {
        viewModelScope.launch {
            when (status) {
                StatusPesanan.DIPROSES -> _pesananDiproses.value =
                    db.pesananDao().getPesananByUserIdAndStatus(userId, StatusPesanan.DIPROSES)
                StatusPesanan.DIKIRIM -> _pesananDikirim.value =
                    db.pesananDao().getPesananByUserIdAndStatus(userId, StatusPesanan.DIKIRIM)
                StatusPesanan.SELESAI -> _pesananSelesai.value =
                    db.pesananDao().getPesananByUserIdAndStatus(userId, StatusPesanan.SELESAI)
                StatusPesanan.DIBATALKAN -> _pesananDibatalkan.value =
                    db.pesananDao().getPesananByUserIdAndStatus(userId, StatusPesanan.DIBATALKAN)
            }
        }
    }
}
