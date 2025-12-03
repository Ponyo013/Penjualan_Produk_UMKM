package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.firestore.model.Ekspedisi
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

// --- PENGGANTI PesananWithItems (ROOM) ---
data class PesananLengkap(
    val pesanan: Pesanan,
    val user: User? = null,
    val items: List<ItemPesanan> = emptyList(),
    val ekspedisi: Ekspedisi? = null
)

class OwnerPesananViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // StateFlow untuk UI List Pesanan (menggunakan model baru PesananLengkap)
    private val _pesananList = MutableStateFlow<List<PesananLengkap>>(emptyList())
    val pesananList: StateFlow<List<PesananLengkap>> get() = _pesananList

    init {
        loadAll()
    }

    // 1. Load Semua Pesanan (Realtime)
    fun loadAll() {
        db.collection("pesanan")
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val rawList = snapshot?.toObjects(Pesanan::class.java)
                    ?.filter { it.status != "KERANJANG" }
                    ?: emptyList()
                fetchDetailsForPesanan(rawList)
            }
    }

    // 2. Load Berdasarkan Status
    fun loadByStatus(status: StatusPesanan) {
        db.collection("pesanan")
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, _ ->
                val now = System.currentTimeMillis()
                val duaJamDalamMillis = 2 * 60 * 60 * 1000L // 2 jam

                val rawList = snapshot?.toObjects(Pesanan::class.java)
                    ?.filter { it.status != "KERANJANG" }
                    ?.filter { pesanan ->
                        val createdAtMillis = pesanan.tanggal.toDate().time
                        when (status) {
                            StatusPesanan.DIPROSES -> {
                                now - createdAtMillis > duaJamDalamMillis
                            }
                            else -> true
                        }
                    }
                    ?: emptyList()
                fetchDetailsForPesanan(rawList)
            }
    }


    // 3. Fungsi Manual untuk Menggabungkan Data (Pesanan + User + Items)
    private fun fetchDetailsForPesanan(pesananList: List<Pesanan>) {
        viewModelScope.launch {
            val resultList = mutableListOf<PesananLengkap>()

            for (pesanan in pesananList) {
                // Ambil User
                var user: User? = null
                try {
                    val userDoc = db.collection("users").document(pesanan.userId).get().await()
                    user = userDoc.toObject(User::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Ambil Item Pesanan
                var items: List<ItemPesanan> = emptyList()
                try {
                    val itemsSnapshot = db.collection("itemPesanan")
                        .whereEqualTo("pesananId", pesanan.id)
                        .get()
                        .await()
                    items = itemsSnapshot.toObjects(ItemPesanan::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                //Ambil ekpedisi
                var ekspedisi: Ekspedisi? = null
                try {
                    val ekspedisiDoc = db.collection("ekspedisi").document(pesanan.ekspedisiId ?: "").get().await()
                    ekspedisi = ekspedisiDoc.toObject(Ekspedisi::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                resultList.add(PesananLengkap(pesanan, user, items, ekspedisi))
            }

            // Update UI setelah semua data terkumpul
            _pesananList.value = resultList
        }
    }

    // 4. Update Status (ID String)
    fun updateStatus(id: String, newStatus: StatusPesanan) {
        db.collection("pesanan").document(id)
            .update("status", newStatus.name)
    }

    // 5. Helper untuk mengambil item spesifik (Dipakai di Card Laporan Penjualan)
    // --- BAGIAN INI YANG TADI TERPOTONG ---
    fun getItemsForPesanan(pesananId: String): LiveData<List<ItemPesanan>> {
        val result = MutableLiveData<List<ItemPesanan>>()
        db.collection("itemPesanan")
            .whereEqualTo("pesananId", pesananId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(ItemPesanan::class.java) ?: emptyList()
                result.value = list
            }
        return result
    }

    // 6. Helper untuk Laporan Keuangan (Semua Item)
    fun getAllItems(): LiveData<List<ItemPesanan>> {
        val result = MutableLiveData<List<ItemPesanan>>()
        db.collection("itemPesanan")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects(ItemPesanan::class.java)
                result.value = list
            }
        return result
    }
}