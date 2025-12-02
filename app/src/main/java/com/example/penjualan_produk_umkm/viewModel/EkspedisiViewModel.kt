package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.firestore.model.Ekspedisi // Pastikan import model yang benar (Firestore)
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class EkspedisiViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("ekspedisi")

    private val _list = MutableLiveData<List<Ekspedisi>>()
    val list: LiveData<List<Ekspedisi>> get() = _list

    private val _addState = MutableLiveData<Boolean>()
    val addState: LiveData<Boolean> get() = _addState

    private val _updateState = MutableLiveData<Boolean>()
    val updateState: LiveData<Boolean> get() = _updateState

    // Init untuk load data pertama kali (opsional, tapi bagus agar data langsung muncul)
    init {
        load()
    }

    fun load() {
        // Mengambil data dari Firestore
        collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Konversi dokumen Firestore ke List object Ekspedisi
                val data = snapshot.toObjects(Ekspedisi::class.java)
                _list.value = data
            }
        }
    }

    fun add(ekspedisi: Ekspedisi) {
        viewModelScope.launch {
            // 1. Buat dokumen baru untuk mendapatkan ID unik
            val newDoc = collection.document()

            // 2. Set ID ke objek ekspedisi
            ekspedisi.id = newDoc.id

            // 3. Simpan ke Firestore
            newDoc.set(ekspedisi)
                .addOnSuccessListener {
                    _addState.value = true
                }
                .addOnFailureListener {
                    _addState.value = false
                }
        }
    }

    fun update(ekspedisi: Ekspedisi) {
        viewModelScope.launch {
            if (ekspedisi.id.isNotEmpty()) {
                collection.document(ekspedisi.id)
                    .set(ekspedisi) // Overwrite data lama dengan yang baru
                    .addOnSuccessListener {
                        _updateState.value = true
                    }
            }
        }
    }

    fun delete(ekspedisi: Ekspedisi) {
        viewModelScope.launch {
            if (ekspedisi.id.isNotEmpty()) {
                collection.document(ekspedisi.id).delete()
            }
        }
    }
}