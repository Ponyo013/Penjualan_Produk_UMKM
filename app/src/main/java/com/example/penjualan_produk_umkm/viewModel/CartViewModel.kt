package com.example.penjualan_produk_umkm.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.viewModelScope

class CartViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // StateFlow untuk menampung list barang
    private val _cartItems = MutableStateFlow<List<ItemPesanan>>(emptyList())
    val cartItems: StateFlow<List<ItemPesanan>> = _cartItems

    // StateFlow untuk data Pesanan (Header)
    private val _pesanan = MutableStateFlow<Pesanan?>(null)
    val pesanan: StateFlow<Pesanan?> = _pesanan

    private var cartListener: ListenerRegistration? = null
    private var pesananListener: ListenerRegistration? = null

    val totalQuantity: StateFlow<Int> = _cartItems.map { list ->
        list.sumOf { it.jumlah }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        initializeCart()
    }
    private fun initializeCart() {
        val userId = auth.currentUser?.uid ?: return

        // Dengarkan perubahan pada collection 'pesanan' milik user ini yang statusnya KERANJANG
        pesananListener = db.collection("pesanan")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", StatusPesanan.KERANJANG.name) // Cari yang status KERANJANG
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CartViewModel", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // A. Jika Ada Keranjang Aktif -> Load data
                    val pesananData = snapshot.documents[0].toObject(Pesanan::class.java)
                    if (pesananData != null) {
                        // Set ID dokumen karena toObject kadang tidak otomatis set ID jika tidak di-map
                        pesananData.id = snapshot.documents[0].id
                        _pesanan.value = pesananData

                        // Mulai dengarkan item di dalam keranjang ini
                        listenToCartItems(pesananData.id)
                    }
                } else {
                    // B. Jika Tidak Ada -> Buat Keranjang Baru
                    createNewCart(userId)
                }
            }
    }

    // 2. Buat Keranjang Baru jika belum ada
    private fun createNewCart(userId: String) {
        val newDoc = db.collection("pesanan").document()
        val newPesanan = Pesanan(
            id = newDoc.id,
            userId = userId,
            totalHarga = 0.0,
            status = StatusPesanan.KERANJANG.name, // Status awal KERANJANG
            tanggal = Timestamp.now()
        )

        newDoc.set(newPesanan)
            .addOnSuccessListener {
                // Setelah sukses dibuat, listener di 'initializeCart' akan otomatis mendeteksi dan memuatnya
            }
    }

    // 3. Dengarkan Item Pesanan secara Realtime
    private fun listenToCartItems(pesananId: String) {
        // Hapus listener lama jika ada (untuk menghindari memory leak/double listen)
        cartListener?.remove()

        cartListener = db.collection("itemPesanan")
            .whereEqualTo("pesananId", pesananId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val items = snapshot.toObjects(ItemPesanan::class.java)

                    // Isi ID manual jika perlu
                    for (i in items.indices) {
                        items[i].id = snapshot.documents[i].id
                    }

                    _cartItems.value = items

                    // Hitung total setiap ada perubahan item
                    recalculateTotal(items, pesananId)
                }
            }
    }

    // 4. Hitung Total Harga (Hanya yang dicentang/isSelected)
    private fun recalculateTotal(items: List<ItemPesanan>, pesananId: String) {
        val currentTotal = items.filter { it.isSelected }
            .sumOf { it.jumlah * it.produkHarga }

        // Cek apakah total berubah, jika ya update ke Firebase
        val currentPesanan = _pesanan.value
        if (currentPesanan != null && currentPesanan.totalHarga != currentTotal) {
            db.collection("pesanan").document(pesananId)
                .update("totalHarga", currentTotal)
        }
    }

    // --- OPERASI ITEM ---

    fun insertItem(item: ItemPesanan) {
        // Pastikan pesananId terisi dengan pesanan yang sedang aktif
        val currentPesananId = _pesanan.value?.id ?: return
        item.pesananId = currentPesananId

        val docRef = db.collection("itemPesanan").document()
        item.id = docRef.id
        docRef.set(item)
    }

    fun updateItem(item: ItemPesanan) {
        if (item.id.isNotEmpty()) {
            db.collection("itemPesanan").document(item.id).set(item)
        }
    }

    fun increaseQuantity(item: ItemPesanan) {
        val newQty = item.jumlah + 1
        // Update jumlah di Firestore
        db.collection("itemPesanan").document(item.id).update("jumlah", newQty)
    }

    fun decreaseQuantity(item: ItemPesanan) {
        if (item.jumlah > 1) {
            val newQty = item.jumlah - 1
            db.collection("itemPesanan").document(item.id).update("jumlah", newQty)
        } else {
            removeItem(item)
        }
    }

    fun removeItem(item: ItemPesanan) {
        db.collection("itemPesanan").document(item.id).delete()
    }

    override fun onCleared() {
        super.onCleared()
        cartListener?.remove()
        pesananListener?.remove()
    }
}