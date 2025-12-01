package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.firestore.model.Ulasan
import com.google.firebase.firestore.FirebaseFirestore

class UlasanViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val ulasanCollection = db.collection("ulasan")

    private val _ulasanList = MutableLiveData<List<Ulasan>>()
    val ulasanList: LiveData<List<Ulasan>> get() = _ulasanList

    // Ambil ulasan untuk produk tertentu (real-time)
    fun getUlasanByProdukId(produkId: String) {
        ulasanCollection
            .whereEqualTo("produkId", produkId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Ulasan::class.java)?.apply { id = doc.id }
                } ?: emptyList()

                _ulasanList.value = list
            }
    }

    // Insert ulasan baru
    fun insertUlasan(ulasan: Ulasan) {
        val newDoc = ulasanCollection.document()
        ulasan.id = newDoc.id
        newDoc.set(ulasan)
    }
}
