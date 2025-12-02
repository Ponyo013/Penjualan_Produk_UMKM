package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.firestore.model.Ulasan
import com.google.firebase.firestore.FirebaseFirestore

class UlasanViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val ulasanCollection = db.collection("ulasan")
    private val produkCollection = db.collection("produk")


    // Pastikan nama variabel ini 'ulasanList' (public)
    private val _ulasanList = MutableLiveData<List<Ulasan>>()
    val ulasanList: LiveData<List<Ulasan>> get() = _ulasanList

    // Ambil ulasan untuk produk tertentu (real-time)
    fun getUlasanByProdukId(produkId: String) {
        ulasanCollection
            .whereEqualTo("produkId", produkId)
            // .orderBy("tanggal", Query.Direction.DESCENDING) // Opsional: Urutkan tanggal
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
            .addOnSuccessListener {
                // Setelah ulasan disimpan → update rating produk
                updateProdukRating(ulasan.produkId)
            }
    }


    fun updateProdukRating(produkId: String) {
        ulasanCollection
            .whereEqualTo("produkId", produkId)
            .get()
            .addOnSuccessListener { query ->
                if (!query.isEmpty) {
                    var total = 0.0
                    for (doc in query.documents) {
                        val rating = doc.getDouble("rating") ?: 0.0
                        total += rating
                    }

                    val rataRata = total / query.size()

                    // Update rating ke tabel produk
                    produkCollection.document(produkId)
                        .update("rating", rataRata)
                } else {
                    // Jika tidak ada ulasan, rating jadi 0
                    produkCollection.document(produkId)
                        .update("rating", 0)
                }
            }

    }

}