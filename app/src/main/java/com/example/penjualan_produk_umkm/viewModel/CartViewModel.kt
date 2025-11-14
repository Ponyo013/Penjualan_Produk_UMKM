package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import com.example.penjualan_produk_umkm.database.model.ItemPesanan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CartViewModel(private val db: AppDatabase, private val pesananId: Int) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<ItemPesananWithProduk>>(emptyList())
    val cartItems: StateFlow<List<ItemPesananWithProduk>> get() = _cartItems

    fun loadCartItems() {
        viewModelScope.launch {
            val items = db.itemPesananDao().getItemsWithProdukByPesananId(pesananId)
            _cartItems.value = items
        }
    }

    fun increaseQuantity(item: ItemPesananWithProduk) {
        viewModelScope.launch {
            val updated = item.itemPesanan.copy(jumlah = item.itemPesanan.jumlah + 1)
            db.itemPesananDao().update(updated)
            loadCartItems()
        }
    }

    fun updateItem(item: ItemPesanan) {
        viewModelScope.launch {
            db.itemPesananDao().update(item)
            loadCartItems()
        }
    }

    fun decreaseQuantity(item: ItemPesananWithProduk) {
        viewModelScope.launch {
            val updatedJumlah = (item.itemPesanan.jumlah - 1).coerceAtLeast(1)
            val updated = item.itemPesanan.copy(jumlah = updatedJumlah)
            db.itemPesananDao().update(updated)
            loadCartItems()
        }
    }

    fun removeItem(item: ItemPesananWithProduk) {
        viewModelScope.launch {
            db.itemPesananDao().delete(item.itemPesanan)
            loadCartItems()
        }
    }
}