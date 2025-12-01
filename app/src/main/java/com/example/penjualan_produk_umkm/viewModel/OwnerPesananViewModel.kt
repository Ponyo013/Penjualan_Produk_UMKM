package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PesananLengkap(
    val pesanan: Pesanan,
    val user: User? = null,
    val items: List<ItemPesanan> = emptyList()
)
class OwnerPesananViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _pesananList = MutableStateFlow<List<PesananLengkap>>(emptyList())
    val pesananList: StateFlow<List<PesananLengkap>> get() = _pesananList

    init {
        loadAll()
    }
    // Ambil semua pesanan
    fun loadAll() {
        db.collection("pesanan")
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val rawList = snapshot?.toObjects(Pesanan::class.java) ?: emptyList()
                fetchDetailsForPesanan(rawList)
            }
    }

    fun loadByStatus(status: StatusPesanan) {
        db.collection("pesanan")
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, _ ->
                val rawList = snapshot?.toObjects(Pesanan::class.java) ?: emptyList()
                fetchDetailsForPesanan(rawList)
            }
    }
    private fun fetchDetailsForPesanan(pesananList: List<Pesanan>) {
        viewModelScope.launch {
            val lengkapList = pesananList.map { pesanan ->
                // Ambil User
                val userSnapshot = db.collection("users").document(pesanan.userId).get().await()
                val user = userSnapshot.toObject(User::class.java)

                // Ambil Items
                val itemsSnapshot = db.collection("itemPesanan")
                    .whereEqualTo("pesananId", pesanan.id)
                    .get().await()
                val items = itemsSnapshot.toObjects(ItemPesanan::class.java)

                PesananLengkap(pesanan, user, items)
            }
            _pesananList.value = lengkapList
        }
    }

    fun updateStatus(id: String, newStatus: StatusPesanan) {
        db.collection("pesanan").document(id).update("status", newStatus.name)
    }
}
