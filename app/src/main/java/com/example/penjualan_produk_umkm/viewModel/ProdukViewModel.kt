package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProdukViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val produkCollection = db.collection("produk")

    private val _allProduk = MutableLiveData<List<Produk>>()
    val allProduk: LiveData<List<Produk>> get() = _allProduk

    init {
        getAllProduk()
    }

    // Ambil semua produk dari Firestore
    fun getAllProduk() {
        produkCollection.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Produk::class.java)?.apply { id = doc.id }
            } ?: emptyList()

            _allProduk.value = list
        }
    }

    // Tambah produk
    fun insertProduk(produk: Produk) {
        viewModelScope.launch {
            val newDoc = produkCollection.document()
            produk.id = newDoc.id
            newDoc.set(produk)
        }
    }

    // Get produk by ID
    fun getProdukById(id: String, callback: (Produk?) -> Unit) {
        produkCollection.document(id).get()
            .addOnSuccessListener { doc ->
                callback(doc.toObject(Produk::class.java))
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    // Update produk
    fun updateProduk(produk: Produk) {
        if (produk.id.isEmpty()) return
        produkCollection.document(produk.id).set(produk)
    }

    // Delete produk
    fun deleteProduk(produk: Produk) {
        if (produk.id.isEmpty()) return
        produkCollection.document(produk.id).delete()
    }
}
