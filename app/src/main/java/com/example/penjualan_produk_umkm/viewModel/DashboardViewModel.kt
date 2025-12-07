package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.firestore.model.Ekspedisi
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // StateFlow untuk menampung list Pesanan dari Firestore
    private val _allPesanan = MutableStateFlow<List<Pesanan>>(emptyList())
    val allPesanan: StateFlow<List<Pesanan>> get() = _allPesanan

    private val _allEkspedisi = MutableStateFlow<List<Ekspedisi>>(emptyList())
    val allEkspedisi: StateFlow<List<Ekspedisi>> get() = _allEkspedisi


    init {
        loadAllPesanan()
        loadAllEkspedisi()
    }

    private fun loadAllEkspedisi() {
        db.collection("ekspedisi")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val list = snapshot.toObjects(Ekspedisi::class.java)
                    _allEkspedisi.value = list
                }
            }
    }

    private fun loadAllPesanan() {
        // Mengambil semua pesanan secara realtime
        db.collection("pesanan")
            .orderBy("tanggal", Query.Direction.DESCENDING) // Urutkan dari terbaru
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.toObjects(Pesanan::class.java)
                    _allPesanan.value = list
                }
            }
    }

    fun hitungOmset(pesananList: List<Pesanan>): Double {
        val mapOngkir = _allEkspedisi.value.associateBy({ it.id }, { it.biaya })

        return pesananList
            .filter { it.status == StatusPesanan.SELESAI.name }
            .sumOf { pesanan ->
                val harga = pesanan.totalHarga
                val ongkir = mapOngkir[pesanan.ekspedisiId] ?: 0.0
                harga - ongkir
            }
    }

}