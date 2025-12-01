package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PesananViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid
        ?: throw IllegalStateException("User belum login")

    private val _pesananDiproses = MutableLiveData<List<Pesanan>>()
    val pesananDiproses: LiveData<List<Pesanan>> get() = _pesananDiproses

    private val _pesananDikirim = MutableLiveData<List<Pesanan>>()
    val pesananDikirim: LiveData<List<Pesanan>> get() = _pesananDikirim

    private val _pesananSelesai = MutableLiveData<List<Pesanan>>()
    val pesananSelesai: LiveData<List<Pesanan>> get() = _pesananSelesai

    private val _pesananDibatalkan = MutableLiveData<List<Pesanan>>()
    val pesananDibatalkan: LiveData<List<Pesanan>> get() = _pesananDibatalkan

    private var listeners: MutableList<ListenerRegistration> = mutableListOf()

    init {
        observePesananByStatus(StatusPesanan.DIPROSES)
        observePesananByStatus(StatusPesanan.DIKIRIM)
        observePesananByStatus(StatusPesanan.SELESAI)
        observePesananByStatus(StatusPesanan.DIBATALKAN)
    }

    private fun observePesananByStatus(status: StatusPesanan) {
        val listener = db.collection("pesanan")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.toObjects(Pesanan::class.java) ?: emptyList()
                when (status) {
                    StatusPesanan.DIPROSES -> _pesananDiproses.value = list
                    StatusPesanan.DIKIRIM -> _pesananDikirim.value = list
                    StatusPesanan.SELESAI -> _pesananSelesai.value = list
                    StatusPesanan.DIBATALKAN -> _pesananDibatalkan.value = list
                }
            }
        listeners.add(listener)
    }

    // Optional: Refresh pesanan secara manual
    fun refreshPesanan(status: StatusPesanan) {
        db.collection("pesanan")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status.name)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects(Pesanan::class.java)
                when (status) {
                    StatusPesanan.DIPROSES -> _pesananDiproses.value = list
                    StatusPesanan.DIKIRIM -> _pesananDikirim.value = list
                    StatusPesanan.SELESAI -> _pesananSelesai.value = list
                    StatusPesanan.DIBATALKAN -> _pesananDibatalkan.value = list
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
    }
}
