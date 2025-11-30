package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import com.example.penjualan_produk_umkm.database.model.ItemPesanan
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val db: AppDatabase, private val pesananId: Int) : ViewModel() {

    // Observe database changes directly using Flow
    val cartItems: StateFlow<List<ItemPesananWithProduk>> =
        db.itemPesananDao().getItemsWithProdukByPesananIdFlow(pesananId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // This function is no longer needed as the UI will collect the flow
    @Deprecated("No longer needed, cartItems Flow updates automatically.")
    fun loadCartItems() {
        // The flow from the DAO will automatically update the cartItems state.
    }

    fun increaseQuantity(item: ItemPesananWithProduk) {
        viewModelScope.launch {
            val updated = item.itemPesanan.copy(jumlah = item.itemPesanan.jumlah + 1)
            db.itemPesananDao().update(updated)
        }
    }

    fun insertItem(item: ItemPesanan) {
        viewModelScope.launch {
            db.itemPesananDao().insert(item)
        }
    }

    fun updateItem(item: ItemPesanan) {
        viewModelScope.launch {
            db.itemPesananDao().update(item)
        }
    }

    fun decreaseQuantity(item: ItemPesananWithProduk) {
        viewModelScope.launch {
            if (item.itemPesanan.jumlah > 1) {
                val updated = item.itemPesanan.copy(jumlah = item.itemPesanan.jumlah - 1)
                db.itemPesananDao().update(updated)
            } else {
                // If quantity is 1, remove the item
                removeItem(item)
            }
        }
    }

    fun removeItem(item: ItemPesananWithProduk) {
        viewModelScope.launch {
            db.itemPesananDao().delete(item.itemPesanan)
        }
    }
}