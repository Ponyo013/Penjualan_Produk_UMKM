package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// Hapus parameter constructor (db, userId, dao) karena kita ambil dari Firebase langsung
class PesananViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Ganti tipe data dari PesananWithItems (Room) ke Pesanan (Firestore)
    private val _pesananDiproses = MutableLiveData<List<Pesanan>>()
    val pesananDiproses: LiveData<List<Pesanan>> get() = _pesananDiproses

    private val _pesananDikirim = MutableLiveData<List<Pesanan>>()
    val pesananDikirim: LiveData<List<Pesanan>> get() = _pesananDikirim

    private val _pesananSelesai = MutableLiveData<List<Pesanan>>()
    val pesananSelesai: LiveData<List<Pesanan>> get() = _pesananSelesai

    private val _pesananDibatalkan = MutableLiveData<List<Pesanan>>()
    val pesananDibatalkan: LiveData<List<Pesanan>> get() = _pesananDibatalkan

    // Untuk menyimpan listener agar bisa dihapus saat ViewModel mati (mencegah memory leak)
    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        if (userId.isNotEmpty()) {
            observePesananByStatus(StatusPesanan.DIPROSES, _pesananDiproses)
            observePesananByStatus(StatusPesanan.DIKIRIM, _pesananDikirim)
            observePesananByStatus(StatusPesanan.SELESAI, _pesananSelesai)
            observePesananByStatus(StatusPesanan.DIBATALKAN, _pesananDibatalkan)
        }
    }

    // Fungsi generik untuk mendengarkan perubahan data realtime
    private fun observePesananByStatus(status: StatusPesanan, liveData: MutableLiveData<List<Pesanan>>) {
        val listener = db.collection("pesanan")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status.name) // Bandingkan String (Enum.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val list = snapshot?.toObjects(Pesanan::class.java) ?: emptyList()
                liveData.value = list
            }
        listeners.add(listener)
    }

    // Mengambil item produk untuk satu pesanan tertentu (Dipakai di Adapter untuk Nested RecyclerView)
    // Mengembalikan List<ItemPesanan> (Model Firestore), bukan ItemPesananWithProduk (Room)
    fun getItemsForPesanan(pesananId: String): LiveData<List<ItemPesanan>> {
        val result = MutableLiveData<List<ItemPesanan>>()

        db.collection("itemPesanan")
            .whereEqualTo("pesananId", pesananId)
            .addSnapshotListener { snapshot, error ->
                if (error == null) {
                    val items = snapshot?.toObjects(ItemPesanan::class.java) ?: emptyList()
                    result.value = items
                }
            }

        return result
    }

    override fun onCleared() {
        super.onCleared()
        // Hapus semua listener saat ViewModel dihancurkan
        listeners.forEach { it.remove() }
    }

    fun cancelPesanan(pesanan: Pesanan, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val batch = db.batch()

        // 1. Update Status Pesanan jadi DIBATALKAN
        val pesananRef = db.collection("pesanan").document(pesanan.id)
        batch.update(pesananRef, "status", StatusPesanan.DIBATALKAN.name)

        // 2. Ambil Item Pesanan untuk mengembalikan Stok
        db.collection("itemPesanan")
            .whereEqualTo("pesananId", pesanan.id)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    val items = snapshot.toObjects(ItemPesanan::class.java)

                    // Loop setiap item, kembalikan stok ke tabel Produk
                    for (item in items) {
                        val produkRef = db.collection("produk").document(item.produkId)
                        // Kembalikan Stok (+), Kurangi Terjual (-)
                        batch.update(produkRef, mapOf(
                            "stok" to com.google.firebase.firestore.FieldValue.increment(item.jumlah.toLong()),
                            "terjual" to com.google.firebase.firestore.FieldValue.increment(-item.jumlah.toLong())
                        ))
                    }

                    // Eksekusi Batch
                    batch.commit()
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError(e.message ?: "Gagal membatalkan") }
                }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal mengambil item") }
    }
}