package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.*
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import com.example.penjualan_produk_umkm.database.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class CheckoutViewModel(
    private val db: AppDatabase,
    private val userId: Int,
    private val pesananId: Int // Kita gunakan ID ini untuk load data & update status
) : ViewModel() {

    // User data
    val user: LiveData<User?> = db.userDao().getUserByIdLive(userId)

    // Item pesanan dengan produk
    private val _itemsWithProduk = MutableLiveData<List<ItemPesananWithProduk>>()
    val itemsWithProduk: LiveData<List<ItemPesananWithProduk>> get() = _itemsWithProduk

    // Ekspedisi aktif
    val ekspedisiAktif: LiveData<List<Ekspedisi>> = db.ekspedisiDao().getActive()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = db.itemPesananDao().getItemsWithProdukByPesananId(pesananId)
            _itemsWithProduk.postValue(items)
            val selectedItems = items.filter { it.itemPesanan.isSelected }
            _itemsWithProduk.postValue(selectedItems)
        }
    }

    // Finalisasi Pesanan (Checkout)
    // Buat pesanan baru (checkout)
    fun createPesanan(
        items: List<ItemPesanan>,
        ekspedisi: Ekspedisi,
        metodePembayaran: MetodePembayaran,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // --- Logika Bisnis (Tetap di IO Thread) ---
                val subtotal = items.sumOf { item ->
                    val produk = db.produkDao().getProdukById(item.produkId)
                    (produk?.harga ?: 0.0) * item.jumlah
                }
                val totalFinal = subtotal + ekspedisi.biaya

                // Update Pesanan yang sudah ada (Keranjang -> DIPROSES)
                val finalPesanan = Pesanan(
                    id = pesananId, // Timpa ID lama (1)
                    userId = userId,
                    status = StatusPesanan.DIPROSES, // Ubah status
                    ekspedisiId = ekspedisi.id,
                    tanggal = LocalDate.now(),
                    metodePembayaran = metodePembayaran,
                    totalHarga = totalFinal
                )

                // Lakukan Update ke Database
                db.pesananDao().update(finalPesanan)

                // --- Pindah ke UI Thread untuk Callback (FIX CRASH) ---
                launch(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // --- Pindah ke UI Thread untuk Callback Error (FIX CRASH) ---
                launch(Dispatchers.Main) {
                    onError(e.message ?: "Gagal memproses pesanan")
                }
            }
        }
    }
    // Update alamat user
    fun updateUserAddress(nama: String, noTelepon: String, alamat: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = db.userDao().getUserById(userId) ?: return@launch
            val updatedUser = currentUser.copy(nama = nama, noTelepon = noTelepon, alamat = alamat)
            db.userDao().update(updatedUser)
        }
    }
}