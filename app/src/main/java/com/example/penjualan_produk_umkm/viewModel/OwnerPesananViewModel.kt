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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                var user: User? = null
                try {
                    val userDoc = db.collection("users").document(pesanan.userId).get().await()
                    user = userDoc.toObject(User::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Ambil Item Pesanan
                var items: List<ItemPesanan> = emptyList()
                try {
                    val itemsSnapshot = db.collection("itemPesanan")
                        .whereEqualTo("pesananId", pesanan.id)
                        .get()
                        .await()
                    items = itemsSnapshot.toObjects(ItemPesanan::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                //Ambil ekpedisi
                var ekspedisi: Ekspedisi? = null
                try {
                    val ekspedisiDoc = db.collection("ekspedisi").document(pesanan.ekspedisiId ?: "").get().await()
                    ekspedisi = ekspedisiDoc.toObject(Ekspedisi::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                resultList.add(PesananLengkap(pesanan, user, items, ekspedisi))
            }

            // Update UI setelah semua data terkumpul
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
                            val subtotal = items.sumOf { it.produkHarga }
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

    // Untuk hasil persentase pendapatan kotor
    fun getPersentasePesananPeriode(startTime: Long, endTime: Long): LiveData<Float> {
        val result = MutableLiveData<Float>()
        db.collection("pesanan")
            .whereGreaterThanOrEqualTo("tanggal", startTime)
            .whereLessThanOrEqualTo("tanggal", endTime)
            .get()
            .addOnSuccessListener { snapshot ->
                val semuaPesanan = snapshot?.toObjects(Pesanan::class.java) ?: emptyList()
                val total = semuaPesanan.size
                val selesai = semuaPesanan.count { it.status == StatusPesanan.SELESAI.name }
                val persentase = if (total > 0) (selesai.toFloat() / total.toFloat()) * 100f else 0f
                result.value = persentase
            }
            .addOnFailureListener {
                result.value = 0f
            }
        return result
    }

    // Mengambil hasil penjualan hari ini
    fun getHasilPenjualanHariIni(): LiveData<Double> {
        val result = MutableLiveData<Double>()
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                // Hitung awal dan akhir hari ini
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = now
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis

                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                calendar.set(java.util.Calendar.MINUTE, 59)
                calendar.set(java.util.Calendar.SECOND, 59)
                calendar.set(java.util.Calendar.MILLISECOND, 999)
                val endOfDay = calendar.timeInMillis

                // Ambil pesanan selesai hari ini
                val semuaPesananHariIni = db.collection("pesanan")
                    .whereGreaterThanOrEqualTo("tanggal", startOfDay)
                    .whereLessThanOrEqualTo("tanggal", endOfDay)
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
                    totalPendapatan += items.sumOf { it.produkHarga }
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
                val calendar = java.util.Calendar.getInstance()

                // Hari kemarin
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfYesterday = calendar.timeInMillis

                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                calendar.set(java.util.Calendar.MINUTE, 59)
                calendar.set(java.util.Calendar.SECOND, 59)
                calendar.set(java.util.Calendar.MILLISECOND, 999)
                val endOfYesterday = calendar.timeInMillis

                val semuaPesananKemarin = db.collection("pesanan")
                    .whereGreaterThanOrEqualTo("tanggal", startOfYesterday)
                    .whereLessThanOrEqualTo("tanggal", endOfYesterday)
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
                    totalPendapatan += items.sumOf { it.produkHarga }
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
                    val cal = java.util.Calendar.getInstance()
                    cal.time = pesanan.tanggal.toDate()
                    "%02d-%02d".format(
                        cal.get(java.util.Calendar.DAY_OF_MONTH),
                        cal.get(java.util.Calendar.MONTH) + 1
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