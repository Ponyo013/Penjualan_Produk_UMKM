package com.example.penjualan_produk_umkm.client.ui.pesanan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.dummyPesanan
import com.example.penjualan_produk_umkm.model.Pesanan
import com.example.penjualan_produk_umkm.model.StatusPesanan

class PesananViewModel : ViewModel() {

    private val _allPesanan = MutableLiveData<List<Pesanan>>(dummyPesanan)

    private val _pesananDiproses = MutableLiveData<List<Pesanan>>().apply {
        value = _allPesanan.value?.filter { it.status == StatusPesanan.DIPROSES }
    }
    val pesananDiproses: LiveData<List<Pesanan>> = _pesananDiproses

    private val _pesananDikirim = MutableLiveData<List<Pesanan>>().apply {
        value = _allPesanan.value?.filter { it.status == StatusPesanan.DIKIRIM }
    }
    val pesananDikirim: LiveData<List<Pesanan>> = _pesananDikirim

    private val _pesananSelesai = MutableLiveData<List<Pesanan>>().apply {
        value = _allPesanan.value?.filter { it.status == StatusPesanan.SELESAI }
    }
    val pesananSelesai: LiveData<List<Pesanan>> = _pesananSelesai
}
