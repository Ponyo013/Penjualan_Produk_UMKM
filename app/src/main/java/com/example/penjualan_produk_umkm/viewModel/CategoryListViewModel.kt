package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.firestore.model.Produk // Pastikan pakai model Firestore
import com.google.firebase.firestore.FirebaseFirestore

class CategoryListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _products = MutableLiveData<List<Produk>>()
    val products: LiveData<List<Produk>> = _products

    // Kita panggil fungsi ini dari Fragment untuk memuat data
    fun loadProdukByCategory(categoryName: String?) {
        // Ambil semua produk dari Firestore (Realtime)
        db.collection("produk").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            // Konversi dokumen ke objek Produk
            val allData = snapshot?.documents?.mapNotNull { doc ->
                val p = doc.toObject(Produk::class.java)
                p?.id = doc.id // Set ID dokumen
                p
            } ?: emptyList()

            // Lakukan Filter di sini
            val filteredList = if (categoryName != null) {
                allData.filter { it.kategori.equals(categoryName, ignoreCase = true) }
            } else {
                allData
            }

            _products.value = filteredList
        }
    }
}