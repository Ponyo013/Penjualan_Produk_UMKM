package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import com.example.penjualan_produk_umkm.database.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.model.Pesanan
import com.example.penjualan_produk_umkm.database.model.StatusPesanan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val db: AppDatabase, private val pesananId: Int) : ViewModel() {

    private val pesananDao = db.pesananDao()

    // Observe database changes directly using Flow for cart items
    val cartItems: StateFlow<List<ItemPesananWithProduk>> =
        db.itemPesananDao().getItemsWithProdukByPesananIdFlow(pesananId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _pesanan = MutableStateFlow<Pesanan?>(null)
    val pesanan: StateFlow<Pesanan?> = _pesanan

    init {
        // Collect changes from cartItems and recalculate total price
        viewModelScope.launch {
            cartItems.collectLatest { _ ->
                recalculateAndSavePesananTotal()
            }
        }

        // Also collect the pesanan itself to keep _pesanan updated
        viewModelScope.launch {
            // Assuming a way to get the current userId is available or can be passed to the ViewModel
            // For now, let's use a placeholder if no actual user login mechanism is integrated yet.
            // You need to replace 'db.userDao().getAllUsersOnce().first().id' with actual current user ID logic.
            val currentUserId = 1 // Placeholder: Replace with actual user ID
            pesananDao.getPendingPesananForUser(currentUserId)?.let { initialPesanan ->
                _pesanan.value = initialPesanan
            }
        }
    }

    fun increaseQuantity(item: ItemPesananWithProduk) {
        viewModelScope.launch {
            val updated = item.itemPesanan.copy(jumlah = item.itemPesanan.jumlah + 1)
            db.itemPesananDao().update(updated)
            // recalculateAndSavePesananTotal() is called via the collectLatest in init block
        }
    }

    fun insertItem(item: ItemPesanan) {
        viewModelScope.launch {
            db.itemPesananDao().insert(item)
            // recalculateAndSavePesananTotal() is called via the collectLatest in init block
        }
    }

    fun updateItem(item: ItemPesanan) {
        viewModelScope.launch {
            db.itemPesananDao().update(item)
            // recalculateAndSavePesananTotal() is called via the collectLatest in init block
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
            // recalculateAndSavePesananTotal() is called via the collectLatest in init block
        }
    }

    fun removeItem(item: ItemPesananWithProduk) {
        viewModelScope.launch {
            db.itemPesananDao().delete(item.itemPesanan)
            // recalculateAndSavePesananTotal() is called via the collectLatest in init block
        }
    }

    private suspend fun recalculateAndSavePesananTotal() {
        // Ensure we always work with the latest Pesanan object
        val currentUserId = 1 // Placeholder: Replace with actual user ID
        val currentPesanan = pesananDao.getPendingPesananForUser(currentUserId)

        if (currentPesanan != null) {
            var newTotalHarga = 0.0
            cartItems.value.filter { it.itemPesanan.isSelected }.forEach { itemWithProduk ->
                newTotalHarga += itemWithProduk.itemPesanan.jumlah * itemWithProduk.produk.harga
            }

            if (currentPesanan.totalHarga != newTotalHarga) {
                val updatedPesanan = currentPesanan.copy(totalHarga = newTotalHarga)
                pesananDao.update(updatedPesanan)
                _pesanan.value = updatedPesanan // Update the StateFlow for UI observation
            } else {
                _pesanan.value = currentPesanan // Ensure UI gets the latest even if total didn't change
            }
        } else {
            _pesanan.value = null // No pending order found
        }
    }
}
