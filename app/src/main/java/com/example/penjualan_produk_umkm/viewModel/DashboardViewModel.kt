package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // pakai Pesanan  saja, karena Dashboard cuma butuh total harga & status
    private val _allPesanan = MutableStateFlow<List<Pesanan>>(emptyList())
    val allPesanan: StateFlow<List<Pesanan>> = _allPesanan

    init {
        db.collection("pesanan").addSnapshotListener { snapshot, _ ->
            val list = snapshot?.toObjects(Pesanan::class.java) ?: emptyList()
            _allPesanan.value = list
        }
    }
}