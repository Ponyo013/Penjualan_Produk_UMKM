package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.firestore.model.Ekspedisi
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.User
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

// --- PENGGANTI PesananWithItems (ROOM) ---
data class PesananLengkap(
    val pesanan: Pesanan,
    val user: User? = null,
    val items: List<ItemPesanan> = emptyList(),
    val ekspedisi: Ekspedisi? = null
)

data class ProdukTerjual(
    val nama: String,
    val stok: Int,
    val jumlahTerjual: Int
)


class OwnerPesananViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // StateFlow untuk UI List Pesanan (menggunakan model baru PesananLengkap)
    private val _pesananList = MutableStateFlow<List<PesananLengkap>>(emptyList())
    val pesananList: StateFlow<List<PesananLengkap>> get() = _pesananList

    private val _produkTerjualList = MutableStateFlow<List<ProdukTerjual>>(emptyList())
    val produkTerjualList: StateFlow<List<ProdukTerjual>> get() = _produkTerjualList

    init {
        loadAll()
    }

    // 1. Load Semua Pesanan (Realtime)
    fun loadAll() {
        db.collection("pesanan")
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val rawList = snapshot?.toObjects(Pesanan::class.java)
                    ?.filter { it.status != "KERANJANG" }
                    ?: emptyList()
                fetchDetailsForPesanan(rawList)
            }
    }

    // 2. Load Berdasarkan Status
    fun loadByStatus(status: StatusPesanan) {
        db.collection("pesanan")
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, _ ->
                val now = System.currentTimeMillis()
                val duaJamDalamMillis = 2 * 60 * 60 * 1000L // 2 jam

                val rawList = snapshot?.toObjects(Pesanan::class.java)
                    ?.filter { it.status != "KERANJANG" }
                    ?.filter { pesanan ->
                        val createdAtMillis = pesanan.tanggal.toDate().time
                        when (status) {
                            StatusPesanan.DIPROSES -> {
                                now - createdAtMillis > duaJamDalamMillis
                            }
                            else -> true
                        }
                    }
                    ?: emptyList()
                fetchDetailsForPesanan(rawList)
            }
    }


    // 3. Fungsi Manual untuk Menggabungkan Data (Pesanan + User + Items)
    private fun fetchDetailsForPesanan(pesananList: List<Pesanan>) {
        viewModelScope.launch {
            val resultList = mutableListOf<PesananLengkap>()

            for (pesanan in pesananList) {
                // Ambil User
                val user: User? = try {
                    db.collection("users").document(pesanan.userId).get().await().toObject(User::class.java)
                } catch (e: Exception) { e.printStackTrace(); null }

                // Ambil Item Pesanan
                var items: List<ItemPesanan> = emptyList()
                try {
                    val itemsSnapshot = db.collection("itemPesanan")
                        .whereEqualTo("pesananId", pesanan.id)
                        .get()
                        .await()
                    items = itemsSnapshot.toObjects(ItemPesanan::class.java)

                    // Ambil gambar untuk tiap item dari produk
                    items = items.map { item ->
                        val gambarUrl = try {
                            val produkDoc = db.collection("produk").document(item.produkId).get().await()
                            val produk = produkDoc.toObject(Produk::class.java)
                            produk?.gambarUrl ?: ""
                        } catch (e: Exception) { e.printStackTrace(); "" }

                        item.copy(gambarUrl = gambarUrl)
                    }
                } catch (e: Exception) { e.printStackTrace() }

                // Ambil ekspedisi
                val ekspedisi: Ekspedisi? = try {
                    db.collection("ekspedisi").document(pesanan.ekspedisiId ?: "").get().await()
                        .toObject(Ekspedisi::class.java)
                } catch (e: Exception) { e.printStackTrace(); null }

                resultList.add(PesananLengkap(pesanan, user, items, ekspedisi))
            }

            // Update UI
            _pesananList.value = resultList
        }
    }


    // 4. Update Status (ID String)
    fun updateStatus(id: String, newStatus: StatusPesanan) {
        db.collection("pesanan").document(id)
            .update("status", newStatus.name)
    }


    // -- View untuk Penjualan --

    // Untuk mengambil value pendaptan kotor
    fun getPendapatanKotor(): LiveData<Double> {
        val result = MutableLiveData<Double>()
        db.collection("pesanan")
            .whereEqualTo("status", StatusPesanan.SELESAI.name)
            .get()
            .addOnSuccessListener { snapshot ->
                val semuaPesanan = snapshot?.toObjects(Pesanan::class.java) ?: emptyList()

                var totalPendapatan = 0.0
                semuaPesanan.forEach { pesanan ->
                    // Ambil semua item untuk pesanan ini
                    db.collection("itemPesanan")
                        .whereEqualTo("pesananId", pesanan.id)
                        .get()
                        .addOnSuccessListener { itemSnapshot ->
                            val items = itemSnapshot.toObjects(ItemPesanan::class.java)
                            val subtotal = items.sumOf { it.produkHarga * it.jumlah}
                            totalPendapatan += subtotal
                            result.value = totalPendapatan
                        }
                }

                // Jika tidak ada pesanan
                if (semuaPesanan.isEmpty()) {
                    result.value = 0.0
                }
            }
            .addOnFailureListener {
                result.value = 0.0
            }
        return result
    }

    fun getPendapatanKotorPeriode(startTime: Long, endTime: Long): LiveData<Double> {
        val result = MutableLiveData<Double>()

        val start = Timestamp(Date(startTime))

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = endTime
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        val end = Timestamp(calendar.time)

        db.collection("pesanan")
            .whereGreaterThanOrEqualTo("tanggal", start)
            .whereLessThanOrEqualTo("tanggal", end)
            .get()
            .addOnSuccessListener { snapshot ->
                val pesananList = snapshot.toObjects(Pesanan::class.java)
                    .filter { it.status == StatusPesanan.SELESAI.name }

                val totalPendapatan = pesananList.sumOf { it.totalHarga ?: 0.0 }

                result.value = totalPendapatan
            }.addOnFailureListener {
                result.value = 0.0
            }

        return result
    }



    // Mengambil hasil penjualan hari ini
    fun getHasilPenjualanHariIni(): LiveData<Double> {
        val result = MutableLiveData<Double>()
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()

                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = Timestamp(calendar.time)

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(java.util.Calendar.MILLISECOND, 999)
                val end = Timestamp(calendar.time)

                // Ambil pesanan selesai hari ini
                val semuaPesananHariIni = db.collection("pesanan")
                    .whereGreaterThanOrEqualTo("tanggal", start)
                    .whereLessThanOrEqualTo("tanggal", end)
                    .get()
                    .await()
                    .toObjects(Pesanan::class.java)
                    .filter { it.status == StatusPesanan.SELESAI.name }

                // Hitung total pendapatan hari ini
                var totalPendapatan = 0.0
                for (pesanan in semuaPesananHariIni) {
                    val items = db.collection("itemPesanan")
                        .whereEqualTo("pesananId", pesanan.id)
                        .get()
                        .await()
                        .toObjects(ItemPesanan::class.java)
                    totalPendapatan += items.sumOf { it.produkHarga * it.jumlah}
                }

                result.value = totalPendapatan

            } catch (e: Exception) {
                e.printStackTrace()
                result.value = 0.0
            }
        }
        return result
    }

    fun getHasilPenjualanHariKemarin(): LiveData<Double> {
        val result = MutableLiveData<Double>()
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()

                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = Timestamp(calendar.time)

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = Timestamp(calendar.time)

                val semuaPesananKemarin = db.collection("pesanan")
                    .whereGreaterThanOrEqualTo("tanggal", start)
                    .whereLessThanOrEqualTo("tanggal", end)
                    .get()
                    .await()
                    .toObjects(Pesanan::class.java)
                    .filter { it.status == StatusPesanan.SELESAI.name }

                var totalPendapatan = 0.0
                for (pesanan in semuaPesananKemarin) {
                    val items = db.collection("itemPesanan")
                        .whereEqualTo("pesananId", pesanan.id)
                        .get()
                        .await()
                        .toObjects(ItemPesanan::class.java)
                    totalPendapatan += items.sumOf { it.produkHarga * it.jumlah}
                }

                result.value = totalPendapatan

            } catch (e: Exception) {
                e.printStackTrace()
                result.value = 0.0
            }
        }
        return result
    }

    // Jumlah Transaksi perhari
    fun getTransaksiPerHari(): LiveData<List<Pair<String, Int>>> {
        val result = MutableLiveData<List<Pair<String, Int>>>()
        viewModelScope.launch {
            try {
                val semuaPesanan = db.collection("pesanan")
                    .get()
                    .await()
                    .toObjects(Pesanan::class.java)
                    .filter { it.status == StatusPesanan.SELESAI.name }

                val grouped = semuaPesanan.groupBy { pesanan ->
                    val cal = Calendar.getInstance()
                    cal.time = pesanan.tanggal.toDate()
                    "%02d-%02d".format(
                        cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.MONTH) + 1
                    )
                }

                val listHarian = grouped.map { (tanggal, listPesanan) ->
                    tanggal to listPesanan.size
                }.sortedBy { it.first }

                result.value = listHarian
            } catch (e: Exception) {
                e.printStackTrace()
                result.value = emptyList()
            }
        }
        return result
    }

    // Mengambil semua produk yang terjual top 3
    fun loadProdukTerjual() {
        viewModelScope.launch {
            try {
                // Ambil semua produk
                val semuaProduk = db.collection("produk")
                    .get()
                    .await()
                    .toObjects(Produk::class.java)

                // Map ke ProdukTerjual dan ambil top 3 berdasarkan terjual
                val topProduk = semuaProduk
                    .sortedByDescending { it.terjual }
                    .take(3)
                    .map { produk ->
                        ProdukTerjual(
                            nama = produk.nama,
                            stok = produk.stok,
                            jumlahTerjual = produk.terjual
                        )
                    }

                _produkTerjualList.value = topProduk

            } catch (e: Exception) {
                e.printStackTrace()
                _produkTerjualList.value = emptyList()
            }
        }
    }

}