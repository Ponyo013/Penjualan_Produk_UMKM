package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.*
import com.example.penjualan_produk_umkm.database.firestore.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CheckoutViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // LiveData user
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    // LiveData cart items
    private val _itemsWithProduk = MutableLiveData<List<ItemPesanan>>()
    val itemsWithProduk: LiveData<List<ItemPesanan>> get() = _itemsWithProduk

    val cartItems: LiveData<List<ItemPesanan>> get() = itemsWithProduk

    // LiveData ekspedisi aktif
    private val _ekspedisiAktif = MutableLiveData<List<Ekspedisi>>()
    val ekspedisiAktif: LiveData<List<Ekspedisi>> get() = _ekspedisiAktif

    // LiveData pesanan
    private val _pesanan = MutableLiveData<Pesanan?>()
    val pesanan: LiveData<Pesanan?> get() = _pesanan

    // Loading & error state
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        currentUserId?.let { uid ->
            loadUser(uid)
            loadPesanan(uid)
            loadActiveEkspedisi()
        }
    }

    /** Load user data */
    private fun loadUser(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) _user.postValue(doc.toObject(User::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Gagal memuat data user: ${e.message}")
            }
        }
    }

    /** Load pesanan aktif */
    private fun loadPesanan(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("pesanan")
                    .whereEqualTo("userId", uid)
                    .whereEqualTo("status", StatusPesanan.DIPROSES)
                    .limit(1)
                    .get()
                    .await()

                val pesananData = snapshot.documents.firstOrNull()?.toObject(Pesanan::class.java)
                _pesanan.postValue(pesananData)

                pesananData?.let { loadItems(it.id) }

            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Gagal memuat pesanan: ${e.message}")
            }
        }
    }

    /** Load item pesanan */
    private fun loadItems(pesananId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("itemPesanan")
                    .whereEqualTo("pesananId", pesananId)
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { it.toObject(ItemPesanan::class.java) }
                _itemsWithProduk.postValue(items.filter { it.isSelected })
            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Gagal memuat item pesanan: ${e.message}")
            }
        }
    }

    /** Load ekspedisi aktif */
    private fun loadActiveEkspedisi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("ekspedisi")
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                val list = snapshot.documents.mapNotNull { it.toObject(Ekspedisi::class.java) }
                _ekspedisiAktif.postValue(list)
            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Gagal memuat ekspedisi: ${e.message}")
            }
        }
    }

    /** Membuat pesanan baru */
    fun createPesanan(
        items: List<ItemPesanan>,
        ekspedisi: Ekspedisi,
        metodePembayaran: MetodePembayaran,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = currentUserId ?: run {
            onError("User belum login")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            try {
                val subtotal = items.sumOf { it.produkHarga * it.jumlah }
                val totalFinal = subtotal + ekspedisi.biaya

                val newPesananRef = db.collection("pesanan").document()
                val finalPesanan = Pesanan(
                    id = newPesananRef.id,
                    userId = uid,
                    status = StatusPesanan.DIPROSES,
                    ekspedisiId = ekspedisi.id,
                    tanggal = Timestamp.now(),
                    metodePembayaran = metodePembayaran,
                    totalHarga = totalFinal
                )

                newPesananRef.set(finalPesanan).await()

                // Update LiveData pesanan
                _pesanan.postValue(finalPesanan)

                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Gagal membuat pesanan: ${e.message}")
                launch(Dispatchers.Main) { onError(e.message ?: "Gagal membuat pesanan") }
            } finally {
                _loading.postValue(false)
            }
        }
    }

    /** Update alamat user dan LiveData */
    fun updateUserAddress(nama: String, noTelepon: String, alamat: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("users").document(uid)
                    .update(
                        mapOf(
                            "nama" to nama,
                            "noTelepon" to noTelepon,
                            "alamat" to alamat
                        )
                    ).await()

                // Update LiveData agar UI langsung berubah
                _user.postValue(_user.value?.copy(
                    nama = nama,
                    noTelepon = noTelepon,
                    alamat = alamat
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Gagal memperbarui alamat: ${e.message}")
            }
        }
    }
}
