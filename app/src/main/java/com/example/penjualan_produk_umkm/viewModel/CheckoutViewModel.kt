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
    private val userId: Int
) : ViewModel() {

    // User data
    val user: LiveData<User?> = db.userDao().getUserByIdLive(userId)

    // Pesanan terakhir
    private val _pesanan = MutableLiveData<Pesanan?>()
    val pesanan: LiveData<Pesanan?> get() = _pesanan

    // Item pesanan dengan produk
    private val _itemsWithProduk = MutableLiveData<List<ItemPesananWithProduk>>()
    val itemsWithProduk: LiveData<List<ItemPesananWithProduk>> get() = _itemsWithProduk
    // Ekspedisi aktif
    val ekspedisiAktif: LiveData<List<Ekspedisi>> = db.ekspedisiDao().getActive()

    init {
        loadLastPesanan()
    }

    private fun loadLastPesanan() {
        viewModelScope.launch(Dispatchers.IO) {
            val pesananList = db.pesananDao().getPesananForUser(userId).value ?: emptyList()
            val lastPesanan = pesananList.lastOrNull()
            _pesanan.postValue(lastPesanan)

            lastPesanan?.let {
                val items = db.itemPesananDao().getItemsWithProdukByPesananId(it.id)
                _itemsWithProduk.postValue(items)
            }
        }
    }

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
                val subtotal = items.sumOf { item ->
                    val produk = db.produkDao().getProdukById(item.produkId)
                    (produk?.harga ?: 0.0) * item.jumlah
                }
                val total = subtotal + ekspedisi.biaya
                val currentUser = db.userDao().getUserById(userId) ?: throw Exception("User tidak ditemukan")
                val newPesanan = Pesanan(
                    userId = currentUser.id,
                    status = StatusPesanan.DIPROSES,
                    ekspedisiId = ekspedisi.id,
                    tanggal = LocalDate.now(),
                    metodePembayaran = metodePembayaran,
                    totalHarga = total,
                )

                val pesananId = db.pesananDao().insert(newPesanan).toInt()
                items.forEach { it.pesananId = pesananId }
                db.itemPesananDao().insertAll(items)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Gagal membuat pesanan")
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
