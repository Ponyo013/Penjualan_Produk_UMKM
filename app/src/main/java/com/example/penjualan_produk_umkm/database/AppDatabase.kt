package com.example.penjualan_produk_umkm.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.dao.*
import com.example.penjualan_produk_umkm.database.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
@Database(
    entities = [
        User::class, Produk::class, Ulasan::class, Pesanan::class,
        ItemPesanan::class, Ekspedisi::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun produkDao(): ProdukDao
    abstract fun ulasanDao(): UlasanDao
    abstract fun pesananDao(): PesananDao
    abstract fun itemPesananDao(): ItemPesananDao
    abstract fun ekspedisiDao(): EkspedisiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "umkm_database"
                )
                    // --- CALLBACK UNTUK SEEDING DATA ---
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Saat database PERTAMA KALI dibuat, jalankan fungsi ini
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    isiDataAwal(database)
                                }
                            }
                        }
                    })
                    // ---------------------------------------------
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Fungsi untuk mengisi data dummy lengkap
        suspend fun isiDataAwal(db: AppDatabase) {
            val userDao = db.userDao()
            val produkDao = db.produkDao()
            val ulasanDao = db.ulasanDao()
            val ekspedisiDao = db.ekspedisiDao()

            // Akun Owner & User
            val owner = User(
                nama = "Boss Sepeda",
                email = "admin@dwiusaha.com",
                password = "123456",
                role = "owner",
                noTelepon = "08123456789",
                alamat = "Kantor Pusat DWI USAHA"
            )
            val user = User(
                nama = "Pembeli Setia",
                email = "user@test.com",
                password = "123456",
                role = "user",
                noTelepon = "08987654321",
                alamat = "Jl. Sudirman No. 10"
            )
            userDao.insert(owner)
            userDao.insert(user)

// 2. Buat Data Produk Dummy (Lengkap & Realistis)
            val daftarProduk = listOf(
                // --- SEPEDA ---
                Produk(
                    nama = "Polygon Premier 5",
                    deskripsi = "Sepeda gunung hardtail yang tangguh untuk segala medan, cocok untuk pemula hingga menengah.",
                    // Perhatikan penggunaan \n untuk baris baru
                    spesifikasi = "Frame: AL6 Alloy\nFork: Suntour XCM 120mm\nGroupset: Shimano Alivio 9x2 Speed\nRem: Hydraulic Disc Brake\nBan: 27.5 Inch",
                    harga = 4750000.0,
                    stok = 5,
                    kategori = "Sepeda",
                    gambarResourceIds = listOf(R.drawable.mtb_polygon),
                    rating = 4.9f,
                    terjual = 25
                ),

                // --- AKSESORIS ---
                Produk(
                    nama = "Helm Rockbros Light",
                    deskripsi = "Helm sepeda aerodinamis dengan lampu belakang LED 3 mode untuk keamanan malam hari.",
                    spesifikasi = "Berat: 230g\nUkuran: All Size (Adjustable)\nBaterai: USB Rechargeable\nMaterial: EPS + PC\nFitur: Lampu LED Belakang",
                    harga = 375000.0,
                    stok = 15,
                    kategori = "Aksesoris",
                    gambarResourceIds = listOf(R.drawable.helm_rockbros),
                    rating = 4.7f,
                    terjual = 150
                ),
                Produk(
                    nama = "Botol Minum Camelbak Podium",
                    deskripsi = "Botol minum sepeda premium dengan teknologi Jet Valve anti tumpah dan isolasi suhu ganda.",
                    spesifikasi = "Kapasitas: 620ml\nMaterial: BPA-Free TruTaste Polypropylene\nFitur: Lock-out dial (Anti Tumpah)\nIsolasi: Double Wall Construction",
                    harga = 245000.0,
                    stok = 40,
                    kategori = "Aksesoris",
                    gambarResourceIds = listOf(R.drawable.botol_camelback),
                    rating = 4.8f,
                    terjual = 88
                ),
                Produk(
                    nama = "Kacamata Rockbros Photochromic",
                    deskripsi = "Kacamata sepeda dengan lensa pintar yang berubah warna menyesuaikan intensitas cahaya matahari.",
                    // INI YANG ANDA MINTA DIPERBAIKI
                    spesifikasi = "Lensa: Photochromic UV400\nFrame: TR90 Ringan & Lentur\nFitur: Anti-fog (Anti Embun)\nNose Pad: Adjustable (Bisa diatur)\nBerat: 30g",
                    harga = 295000.0,
                    stok = 20,
                    kategori = "Aksesoris",
                    gambarResourceIds = listOf(R.drawable.kacamata_rockbros),
                    rating = 4.6f,
                    terjual = 210
                ),
                Produk(
                    nama = "Lampu Depan Cateye VOLT400",
                    deskripsi = "Lampu depan profesional dengan kecerahan tinggi, sangat direkomendasikan untuk night ride.",
                    spesifikasi = "Output: 400 Lumens\nBaterai: Li-ion Rechargeable (Tahan 6 jam)\nMode: 5 mode pencahayaan\nMount: FlexTight Bracket",
                    harga = 850000.0,
                    stok = 8,
                    kategori = "Aksesoris",
                    gambarResourceIds = listOf(R.drawable.lampudepan_cateye),
                    rating = 5.0f,
                    terjual = 12
                ),

                // --- SPARE PARTS ---
                Produk(
                    nama = "Ban Luar Maxxis Ardent",
                    deskripsi = "Ban MTB performa tinggi dengan traksi maksimal di jalan tanah dan bebatuan.",
                    spesifikasi = "Ukuran: 27.5 x 2.25\nTipe: Wire Bead\nKompon: Dual Compound\nTekanan: Max 60 PSI\nBerat: 750g",
                    harga = 210000.0,
                    stok = 50,
                    kategori = "Spare Parts",
                    gambarResourceIds = listOf(R.drawable.banluar_maxxis_1),
                    rating = 4.9f,
                    terjual = 340
                ),
                Produk(
                    nama = "Brake Pad Shimano B01S",
                    deskripsi = "Kampas rem cakram berbahan resin, memberikan pengereman senyap dan modulasi yang baik.",
                    spesifikasi = "Material: Resin (Organik)\nKompatibel: MT200, M315, M355, M395\nIsi Paket: 1 pasang + per + split pin\nFitur: Low Noise",
                    harga = 95000.0,
                    stok = 100,
                    kategori = "Spare Parts",
                    gambarResourceIds = listOf(R.drawable.brake_pad_2),
                    rating = 4.8f,
                    terjual = 500
                ),
                Produk(
                    nama = "Grip Handlebar ODI Elite",
                    deskripsi = "Handgrip sepeda anti-slip dengan sistem lock-on ganda, nyaman digenggam saat berkeringat.",
                    spesifikasi = "Material: Soft Rubber Compound\nPanjang: 130mm\nDiameter: 22.2mm (Standar)\nFitur: Double Lock Ring (Anti Muter)",
                    harga = 150000.0,
                    stok = 30,
                    kategori = "Spare Parts",
                    gambarResourceIds = listOf(R.drawable.grip_handlebar),
                    rating = 4.5f,
                    terjual = 65
                ),
                Produk(
                    nama = "Pedal Cleat Shimano PD-M520",
                    deskripsi = "Pedal cleat MTB legendaris yang tahan lumpur dan sangat awet, cocok untuk kompetisi.",
                    spesifikasi = "Sistem: SPD (Shimano Pedaling Dynamics)\nBerat: 380g/pasang\nMaterial: Alloy Body & Cr-Mo Axle\nFitur: Mud shedding design",
                    harga = 575000.0,
                    stok = 12,
                    kategori = "Spare Parts",
                    gambarResourceIds = listOf(R.drawable.pedal_cleat_2),
                    rating = 4.9f,
                    terjual = 45
                ),
                Produk(
                    nama = "Rantai KMC X9 (9 Speed)",
                    deskripsi = "Rantai sepeda 9 percepatan dengan teknologi X-Bridge untuk perpindahan gigi yang presisi.",
                    spesifikasi = "Speed: 9 Speed\nLink: 116L\nWarna: Silver/Grey\nKompatibel: Shimano, SRAM, Campagnolo\nTeknologi: X-Bridge",
                    harga = 185000.0,
                    stok = 45,
                    kategori = "Spare Parts",
                    gambarResourceIds = listOf(R.drawable.rantai_kmc_2),
                    rating = 4.7f,
                    terjual = 220
                )
            )

            // Masukkan semua produk ke database
            daftarProduk.forEach { produk ->
                produkDao.insert(produk)
            }

            // 3. Isi Ekspedisi Default
            val defaultsExp = listOf(
                Ekspedisi(0, "JNE", "JNE", 3, 15000.0, "Reguler"),
                Ekspedisi(0, "J&T", "JNT", 2, 18000.0, "Express"),
                Ekspedisi(0, "SiCepat", "SICEPAT", 2, 16000.0, "Reg")
            )
            defaultsExp.forEach { ekspedisiDao.insert(it) }
        }

        // Fungsi Helper untuk generate ulasan spesifik
        // Di dalam AppDatabase.kt, bagian paling bawah

        private fun getReviewsForProduct(produkId: Int, namaProduk: String, userId: Int): List<Ulasan> {
            val today = LocalDate.now()

            val name = namaProduk.lowercase()

            return when {
                name.contains("polygon") -> listOf(
                    Ulasan(0, produkId, userId, 5.0f, "Sepeda impian! Pengiriman aman dan rakitnya gampang.", today.minusDays(2)),
                    Ulasan(0, produkId, userId, 4.0f, "Barang bagus, cuma settingan RD perlu disetel dikit.", today.minusDays(10))
                )
                name.contains("rockbros") && name.contains("helm") -> listOf(
                    Ulasan(0, produkId, userId, 5.0f, "Lampu belakangnya terang banget, safety buat night ride.", today.minusDays(1)),
                    Ulasan(0, produkId, userId, 5.0f, "Ringan di kepala, busanya empuk.", today.minusDays(5))
                )
                name.contains("camelbak") -> listOf(
                    Ulasan(0, produkId, userId, 5.0f, "Beneran ga bau plastik airnya. Jet valve nya anti tumpah.", today.minusDays(3)),
                    Ulasan(0, produkId, userId, 4.0f, "Agak susah dibersihin bagian tutupnya, tapi overall oke.", today.minusDays(15))
                )
                name.contains("kacamata") -> listOf(
                    Ulasan(0, produkId, userId, 4.0f, "Photochromic-nya berfungsi baik, kena matahari langsung gelap.", today.minusDays(4)),
                    Ulasan(0, produkId, userId, 5.0f, "Frame lentur, lensa jernih. Worth it!", today.minusDays(8))
                )
                name.contains("cateye") -> listOf(
                    Ulasan(0, produkId, userId, 5.0f, "Terang pol! Kayak lampu motor. Baterai awet.", today.minusDays(6))
                )
                name.contains("maxxis") -> listOf(
                    Ulasan(0, produkId, userId, 5.0f, "Grip di tanah basah ngeri, ga licin sama sekali.", today.minusDays(7)),
                    Ulasan(0, produkId, userId, 5.0f, "Ban sejuta umat MTB, kualitas ga diragukan.", today.minusDays(20))
                )
                name.contains("brake") -> listOf(
                    Ulasan(0, produkId, userId, 5.0f, "Pengereman jadi pakem dan senyap, ga bunyi cit cit lagi.", today.minusDays(2))
                )
                name.contains("grip") -> listOf(
                    Ulasan(0, produkId, userId, 4.0f, "Karetnya empuk dan lengket, tangan ga gampang kesemutan.", today.minusDays(5))
                )
                name.contains("pedal") -> listOf(
                    Ulasan(0, produkId, userId, 5.0f, "Cleat mechanism nya loncer, masuk keluar sepatu gampang.", today.minusDays(9)),
                    Ulasan(0, produkId, userId, 5.0f, "Tahan banting, kena batu aman.", today.minusDays(30))
                )
                name.contains("rantai") || name.contains("kmc") -> listOf(
                    Ulasan(0, produkId, userId, 5.0f, "Pindah gigi jadi smooth banget pake rantai ini.", today.minusDays(3))
                )
                else -> listOf(
                    Ulasan(0, produkId, userId, 5.0f, "Produk sangat berkualitas! Recommended seller.", today.minusDays(1))
                )
            }
        }
    }
}