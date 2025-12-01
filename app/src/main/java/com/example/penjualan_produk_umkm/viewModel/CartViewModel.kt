package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CartViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    private val _cartItems = MutableStateFlow<List<ItemPesanan>>(emptyList())
    val cartItems: StateFlow<List<ItemPesanan>> = _cartItems

    private val _pesanan = MutableStateFlow<Pesanan?>(null)
    val pesanan: StateFlow<Pesanan?> = _pesanan

    private var cartListener: ListenerRegistration? = null
    private var pesananListener: ListenerRegistration? = null

    init {
        observePesanan()
    }

    private fun observePesanan() {
        pesananListener?.remove()
        pesananListener = db.collection("pesanan")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "DIPROSES")
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val pesananData = snapshot?.documents?.firstOrNull()?.toObject(Pesanan::class.java)
                _pesanan.value = pesananData
                pesananData?.let { observeCartItems(it.id) }
            }
    }

    private fun observeCartItems(pesananId: String) {
        cartListener?.remove()
        cartListener = db.collection("itemPesanan")
            .whereEqualTo("pesananId", pesananId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val items = snapshot?.toObjects(ItemPesanan::class.java) ?: emptyList()
                _cartItems.value = items
                recalculatePesananTotal(items)
            }
    }

    private fun recalculatePesananTotal(items: List<ItemPesanan>) {
        val pesananData = _pesanan.value ?: return
        val total = items.filter { it.isSelected }.sumOf { it.jumlah * it.produkHarga }
        if (pesananData.totalHarga != total) {
            val updated = pesananData.copy(totalHarga = total)
            db.collection("pesanan").document(updated.id).set(updated)
            _pesanan.value = updated
        }
    }

    fun increaseQuantity(item: ItemPesanan) = updateQuantity(item, item.jumlah + 1)

    fun decreaseQuantity(item: ItemPesanan) {
        if (item.jumlah > 1) updateQuantity(item, item.jumlah - 1)
        else removeItem(item)
    }

    fun updateItem(item: ItemPesanan) {
        db.collection("itemPesanan").document(item.id)
            .set(item)
    }

    private fun updateQuantity(item: ItemPesanan, newQty: Int) {
        db.collection("itemPesanan").document(item.id)
            .update("jumlah", newQty)
    }

    fun removeItem(item: ItemPesanan) {
        db.collection("itemPesanan").document(item.id).delete()
    }

    fun insertItem(item: ItemPesanan) {
        val docRef = db.collection("itemPesanan").document()
        item.id = docRef.id
        docRef.set(item)
    }

    override fun onCleared() {
        super.onCleared()
        cartListener?.remove()
        pesananListener?.remove()
    }
}
