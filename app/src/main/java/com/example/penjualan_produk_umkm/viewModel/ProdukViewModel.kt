package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.google.firebase.firestore.FirebaseFirestore // Tambahkan ini
import kotlinx.coroutines.launch

class ProdukViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val produkCollection = db.collection("produk")
    private val pesananItemCollection = db.collection("itemPesanan")

    private val _allProduk = MutableLiveData<List<Produk>>()
    val allProduk: LiveData<List<Produk>> = _allProduk

    init {
        getAllProduk()
    }

    // 1. Ambil Data Realtime
    fun getAllProduk() {
        produkCollection.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            val list = snapshot?.documents?.mapNotNull { doc ->
                val p = doc.toObject(Produk::class.java)
                p?.id = doc.id // Set ID dokumen ke objek agar bisa di-edit/delete
                p
            } ?: emptyList()

            _allProduk.value = list
        }
    }

    // 2. Tambah Produk
    fun insertProduk(produk: Produk) {
        viewModelScope.launch {
            // Buat dokumen baru kosong untuk dapat ID
            val newDoc = produkCollection.document()
            produk.id = newDoc.id
            // Simpan data
            newDoc.set(produk)
        }
    }

    // 3. Update Produk
    fun updateProduk(produk: Produk) {
        if (produk.id.isNotEmpty()) {
            produkCollection.document(produk.id).set(produk)
        }
    }

    // 4. Hapus Produk + Item Keranjang
    fun deleteProduk(produk: Produk) {
        val produkId = produk.id
        if (produkId.isEmpty()) return

        // Hapus produk
        produkCollection.document(produkId).delete()
            .addOnSuccessListener {
                // Setelah produk dihapus → hapus semua item di keranjang
                deleteProdukFromCart(produkId)
            }
    }

    // Fungsi untuk menghapus produk dari semua keranjang
    private fun deleteProdukFromCart(produkId: String) {
        pesananItemCollection
            .whereEqualTo("produkId", produkId)
            .whereEqualTo("status", "KERANJANG") // hanya menghapus produk di keranjang
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot == null) return@addSnapshotListener

                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
            }
    }


    // 5. Get by ID (Untuk Edit/Detail)
    fun getProdukById(id: String, onResult: (Produk?) -> Unit) {
        produkCollection.document(id).get()
            .addOnSuccessListener { document ->
                val p = document.toObject(Produk::class.java)
                p?.id = document.id
                onResult(p)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }


}