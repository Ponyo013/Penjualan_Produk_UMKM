package com.example.penjualan_produk_umkm.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.example.penjualan_produk_umkm.database.firestore.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

    // Karena kita pakai model ItemPesanan langsung, cartItems sama dengan itemsWithProduk
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

    /** Load pesanan aktif (Status: DIPROSES tapi belum final checkout, dianggap Keranjang di logic baru) */
    private fun loadPesanan(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Cari pesanan yang statusnya DIPROSES (sebagai keranjang aktif)
                val snapshot = db.collection("pesanan")
                    .whereEqualTo("userId", uid)
                    .whereEqualTo("status", StatusPesanan.KERANJANG.name) // Menggunakan String Name
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

    /** Load item pesanan berdasarkan ID Pesanan */
    private fun loadItems(pesananId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("itemPesanan")
                    .whereEqualTo("pesananId", pesananId)
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { it.toObject(ItemPesanan::class.java) }
                // Hanya ambil item yang dicentang (isSelected = true) untuk checkout
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

    /** Finalisasi Pesanan (Checkout) */
    // Logika baru: Kita UPDATE pesanan yang sudah ada, bukan buat baru, agar ID tetap sama
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

        val currentPesananId = _pesanan.value?.id ?: run {
            // Jika null, buat baru (fallback)
            val newRef = db.collection("pesanan").document()
            newRef.id
        }

        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            try {
                // Hitung total harga final
                val subtotal = items.sumOf { it.produkHarga * it.jumlah }
                val totalFinal = subtotal + ekspedisi.biaya

                val userAddress = _user.value?.alamat ?: ""

                // Update data pesanan di Firestore
                val updates = mapOf(
                    "status" to StatusPesanan.DIPROSES.name, // Pastikan status "Deal"
                    "ekspedisiId" to ekspedisi.id,
                    "metodePembayaran" to metodePembayaran, // Firestore menyimpan enum sebagai map/string tergantung config, aman pakai object mapper
                    "totalHarga" to totalFinal,
                    "tanggal" to Timestamp.now(), // Update tanggal transaksi
                    "alamat" to userAddress
                )

                // Lakukan update
                db.collection("pesanan").document(currentPesananId)
                    .update(updates)
                    .await()

                val batch = db.batch()

                items.forEach { item ->
                    val produkRef = db.collection("produk").document(item.produkId)

                    batch.update(produkRef, mapOf(
                        "stok" to FieldValue.increment(-item.jumlah.toLong()),
                        "terjual" to FieldValue.increment(item.jumlah.toLong())
                    ))
                }

                batch.commit().await()

                // Kirim notifikasi ke admin
                sendNotificationToAdmin(currentPesananId, totalFinal, _user.value?.nama)

                // Kembali ke UI Thread
                launch(Dispatchers.Main) { onSuccess() }

            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Gagal checkout: ${e.message}")
                launch(Dispatchers.Main) { onError(e.message ?: "Gagal checkout") }
            } finally {
                _loading.postValue(false)
            }
        }
    }

    private fun sendNotificationToAdmin(pesananId: String, totalHarga: Double, userName: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val formattedTotal = "Rp ${String.format("%,.2f", totalHarga)}"
                val notificationMessage = if (userName != null) {
                    "Pesanan baru dari $userName sejumlah $formattedTotal dengan ID: $pesananId"
                } else {
                    "Pesanan baru sejumlah $formattedTotal dengan ID: $pesananId"
                }

                val notification = hashMapOf(
                    "title" to "Pesanan Baru Diterima",
                    "message" to notificationMessage,
                    "timestamp" to System.currentTimeMillis(),
                    "readStatus" to false,
                    "recipient" to "admin" // To identify this is for admin
                )

                db.collection("notifications")
                    .add(notification)
                    .await()
            } catch (e: Exception) {
                e.printStackTrace()
                // Log the error but don't block the UI
                Log.e("CheckoutViewModel", "Gagal mengirim notifikasi admin", e)
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

                // Update LiveData agar UI langsung berubah tanpa fetch ulang
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

    private fun updateProdukStockAndSold(items: List<ItemPesanan>) {
        val produkRef = db.collection("produk")

        items.forEach { item ->
            val docRef = produkRef.document(item.produkId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentStock = snapshot.getLong("stok") ?: 0
                val currentTerjual = snapshot.getLong("terjual") ?: 0

                val newStock = currentStock - item.jumlah

                val newTerjual = currentTerjual + item.jumlah

                // Cegah stock minus
                if (newStock < 0) {
                    throw Exception("Stok tidak cukup")
                }

                transaction.update(docRef, mapOf(
                    "stok" to newStock,
                    "terjual" to newTerjual
                ))
            }
        }
    }

}