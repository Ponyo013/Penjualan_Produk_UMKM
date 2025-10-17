package com.example.penjualan_produk_umkm

import androidx.compose.runtime.mutableStateListOf
import com.example.penjualan_produk_umkm.model.*
import org.threeten.bp.LocalDate

// Update data dummy
val produkDummyList = mutableStateListOf(
    // =========================================================================
    // PRODUK 1: SEPEDA GUNUNG (POLYGON PREMIER 5)
    // =========================================================================
    Produk(
        id = 1,
        nama = "MTB Polygon Premier 5 (27.5\")",
        deskripsi = "Polygon Premier 5 adalah Mountain Bike (MTB) serbaguna yang ideal untuk penggunaan harian dan *light off-road*. Dibuat dengan rangka AL6 XC Sport yang ringan dan geometris untuk kenyamanan berkendara, sepeda ini dilengkapi dengan suspensi depan Suntour XCM yang dapat mengunci (lockout) dan sistem pengereman hidrolik, memberikan kontrol superior di medan yang bervariasi. Cocok untuk *beginner* hingga *intermediate* trail riding.",
        spesifikasi = "Rangka: AL6 XC Sport\nGigi: Shimano Alivio/Acera Mix 2x9 Speed\nSuspensi: Suntour XCM HLO Fork (Travel 120mm, Lockout)\nRem: Tektro HD-M275 Hydraulic Disc Brake\nRoda: 27.5 inci",
        harga = 4800000.0,
        stok = 8,
        kategori = "Sepeda Gunung",
        gambarResourceIds = listOf(
            R.drawable.mtb_polygon
        ),
        rating = 4.7f,
        terjual = 25
    ),

    // =========================================================================
    // PRODUK 2: HELM (ROCKBROS ZK-013)
    // =========================================================================
    Produk(
        id = 2,
        nama = "Helm ROCKBROS ZK-013 (LED)",
        deskripsi = "ROCKBROS ZK-013 adalah helm sepeda multifungsi untuk MTB dan *road cycling*, dilengkapi dengan LED lampu belakang yang meningkatkan visibilitas dan keamanan saat berkendara malam hari. Helm ini dirancang dengan bahan PC di bagian luar dan EPS + PU foam di bagian dalam untuk proteksi maksimal terhadap benturan. Fitur *waterproof*, *shock resistance*, dan *integrally molded construction* memastikan helm tetap ringan sekaligus kuat.",
        spesifikasi = "Brand: ROCKBROS\nTipe: ZK-013\nBerat Bersih: 252g\nKonstruksi: In-Mold Shell + EPS Foam\nVentilasi: 18 Lubang Udara\nLampu Belakang: 2000 mAh Rechargeable LED (USB Charging)\nSertifikasi: SNI, CE",
        harga = 250000.0,
        stok = 35,
        kategori = "Helm",
        gambarResourceIds = listOf(
            R.drawable.helm_rockbros
        ),
        rating = 4.9f,
        terjual = 40
    ),

    // =========================================================================
    // PRODUK 3: SPARE PART (RANTAI KMC X9)
    // =========================================================================
    Produk(
        id = 3,
        nama = "Rantai KMC X9 (9 Speed)",
        deskripsi = "Rantai KMC X9 dirancang untuk sistem transmisi 9 *speed* pada sepeda gunung maupun *road bike*. Dengan teknologi X-Bridge, rantai ini memastikan perpindahan gigi yang cepat, mulus, dan responsif. Konstruksi yang kuat dan tahan lama, serta lapisan anti-karat membuatnya ideal untuk penggunaan intensif di berbagai kondisi cuaca.",
        spesifikasi = "Brand: KMC\nTipe: X9\nKompatibilitas: 9 Speed Shimano/SRAM\nPanjang: 116 Tautan\nFitur: X-Bridge Design",
        harga = 185000.0,
        stok = 50,
        kategori = "Spare Parts",
        gambarResourceIds = listOf(
            R.drawable.rantai_kmc_1,
            R.drawable.rantai_kmc_2
        ),
        rating = 4.6f,
        terjual = 65
    ),

    // =========================================================================
    // PRODUK 4: BOTOL MINUM (CAMELBAK PODIUM)
    // =========================================================================
    Produk(
        id = 4,
        nama = "Botol Minum CamelBak Podium",
        deskripsi = "Botol minum CamelBak Podium 710ml adalah pilihan utama bagi pesepeda. Dibuat dengan material TruTaste™ Polypropylene yang 100% bebas BPA, botol ini memastikan air minum Anda bebas dari rasa plastik. Fitur Jet Valve™ yang mengunci otomatis mencegah tumpahan dan mempermudah hidrasi dengan satu tangan saat sedang bergerak.",
        spesifikasi = "Merek: CamelBak\nTipe: Podium (Insulated)\nVolume: 710 ml\nBahan: TruTaste™ Polypropylene (BPA-Free)\nFitur: Self-Sealing Jet Valve™, Hydroguard™ Anti-Microbial",
        harga = 180000.0,
        stok = 40,
        kategori = "Aksesoris",
        gambarResourceIds = listOf(
            R.drawable.botol_camelback
        ),
        rating = 4.8f,
        terjual = 70
    ),

    // =========================================================================
    // PRODUK 5: LAMPU SEPEDA (CATEYE VOLT 400)
    // =========================================================================
    Produk(
        id = 5,
        nama = "Lampu Depan CatEye Volt 400",
        deskripsi = "CatEye Volt 400 adalah lampu sepeda depan (headlight) yang ringkas namun bertenaga, ideal untuk penggunaan di kota dan sesi latihan malam hari. Menawarkan output cahaya 400 lumens dengan beberapa mode pencahayaan, lampu ini mudah diisi ulang via USB dan memiliki indikator baterai rendah. Pemasangan fleksibel dan cepat tanpa alat.",
        spesifikasi = "Merek: CatEye\nTipe: Volt 400\nOutput: 400 Lumens Max\nBaterai: Li-ion Rechargeable (USB)\nWaktu Penggunaan: Hingga 60 jam (Mode Kedip)\nFitur: Low Battery Indicator",
        harga = 650000.0,
        stok = 15,
        kategori = "Aksesoris",
        gambarResourceIds = listOf(
            R.drawable.lampudepan_cateye
        ),
        rating = 4.3f,
        terjual = 30
    ),

    // =========================================================================
    // PRODUK 6: SPARE PART (PEDAL SHIMANO) - Terjual Sedikit
    // =========================================================================
    Produk(
        id = 6,
        nama = "Pedal Cleat Shimano SPD M520",
        deskripsi = "Pedal Shimano M520 adalah pedal clip-less (SPD) serbaguna dan tahan lumpur, ideal untuk MTB, Touring, atau penggunaan commuting harian. Desain dua sisi (dual-sided) memungkinkan pemasangan cleat yang mudah dan cepat. Pedal ini sangat andal dengan poros CroMo (Chromoly steel) dan *sealed bearings* yang minim perawatan.",
        spesifikasi = "Merek: Shimano\nTipe: PD-M520 (SPD)\nBerat: 380g per pasang\nMaterial: Aluminium Body, CroMo Spindle\nFitur: Dual-Sided, Adjustable Release Tension",
        harga = 550000.0,
        stok = 25,
        kategori = "Spare Parts",
        gambarResourceIds = listOf(
            R.drawable.pedal_cleat_1,
            R.drawable.pedal_cleat_2
        ),
        rating = 4.8f,
        terjual = 18 // Terjual rendah
    ),

    // =========================================================================
    // PRODUK 7: SPARE PART (BAN LUAR MAXXIS) - Terjual Sedikit
    // =========================================================================
    Produk(
        id = 7,
        nama = "Ban Luar Maxxis Ikon 27.5x2.20",
        deskripsi = "Ban Maxxis Ikon dirancang untuk kecepatan dan traksi. Pola tapak yang cepat bergulir sangat cocok untuk kondisi tanah kering hingga sedang. Teknologi 3C MaxxSpeed Compound memberikan cengkeraman maksimal tanpa mengorbankan kecepatan. Ban ini adalah favorit untuk *cross-country (XC) racing*.",
        spesifikasi = "Merek: Maxxis\nTipe: Ikon\nUkuran: 27.5 x 2.20\nJenis: Folding (Tubeless Ready)\nCompound: 3C MaxxSpeed",
        harga = 680000.0,
        stok = 12,
        kategori = "Spare Parts",
        gambarResourceIds = listOf(
            R.drawable.banluar_maxxis_1,
            R.drawable.banluar_maxxis_2
        ),
        rating = 4.6f,
        terjual = 15 // Terjual rendah
    ),

    // =========================================================================
    // PRODUK 8: SPARE PART (BRAKE PAD/KANVAS REM) - Terjual Sedikit
    // =========================================================================
    Produk(
        id = 8,
        nama = "Brake Pad Shimano Resin (B01S)",
        deskripsi = "Kanvas Rem Resin Shimano B01S memberikan pengereman yang halus dan tenang. Ideal untuk penggunaan umum dan memiliki umur pemakaian yang baik. Kompatibel dengan banyak seri *brake caliper* Shimano (seperti Altus, Acera, Alivio) yang menggunakan sistem hidrolik/mekanik.",
        spesifikasi = "Merek: Shimano\nTipe: B01S (Resin Pad)\nKompatibilitas: M315, M395, M446, M525, dst.\nFitur: Low Noise, Excellent Initial Bite",
        harga = 85000.0,
        stok = 30,
        kategori = "Spare Parts",
        gambarResourceIds = listOf(
            R.drawable.brake_pad,
            R.drawable.brake_pad_2
        ),
        rating = 4.0f,
        terjual = 20 // Terjual Sedikit (Tepat di batas filter > 20)
    ),

    // =========================================================================
    // PRODUK 9: AKSESORIS (KACAMATA SEPEDA) - Terjual Sedikit
    // =========================================================================
    Produk(
        id = 9,
        nama = "Kacamata Sepeda Rockbros Polarized",
        deskripsi = "Kacamata dengan lensa polarized yang mengurangi silau dan memberikan perlindungan 100% terhadap sinar UV400. Frame ringan dan fleksibel, nyaman dipakai untuk durasi lama. Ideal untuk bersepeda, memancing, atau aktivitas luar ruangan lainnya.",
        spesifikasi = "Merek: Rockbros\nLensa: Polarized, UV400 Protection\nFitur: 5 Lensa Interchangeable\nMaterial: TR90 Frame, Polycarbonate Lens",
        harga = 190000.0,
        stok = 20,
        kategori = "Aksesoris",
        gambarResourceIds = listOf(
            R.drawable.kacamata_rockbros
        ),
        rating = 4.4f,
        terjual = 19 // Terjual rendah
    ),

    // =========================================================================
    // PRODUK 10: AKSESORIS (GRIP HANDLEBAR) - Terjual Sedikit
    // =========================================================================
    Produk(
        id = 10,
        nama = "Grip Handlebar Lock-On Ergonomic",
        deskripsi = "Grip ergonomis yang dirancang untuk mengurangi kelelahan tangan pada perjalanan jauh. Dilengkapi dengan sistem *lock-on* yang memastikan grip tidak berputar atau bergeser selama penggunaan intensif. Material karet anti-slip memberikan kontrol maksimal.",
        spesifikasi = "Tipe: Ergonomic, Lock-On\nPanjang: 130mm\nDiameter: Standar 22mm\nMaterial: Rubber Compound, Aluminium Clamp",
        harga = 75000.0,
        stok = 40,
        kategori = "Aksesoris",
        gambarResourceIds = listOf(
            R.drawable.grip_handlebar
        ),
        rating = 4.1f,
        terjual = 20 // Terjual Sedikit (Tepat di batas filter > 20)
    ),
)

val dummyUsers = mutableMapOf(
    "user1@example.com" to User(1, "Andi", "user1@example.com", "password123", "customer", "081234567890", "Jl. Melati No. 10, Jakarta"),
    "user2@example.com" to User(2, "Budi", "user2@example.com", "password123", "customer", "081234567891", "Jl. Mawar No. 20, Bandung"),
    "user3@example.com" to User(3, "Citra", "user3@example.com", "password123", "customer", "081234567892", "Jl. Kenanga No. 30, Surabaya")
)

val ekspedisiDummy = mutableStateListOf(
    Ekspedisi(1, "JNE", "jne", 2, 15000.0, "Reguler"),
    Ekspedisi(2, "J&T", "jnt", 1, 20000.0, "Express"),
    Ekspedisi(3, "SiCepat", "sicepat", 3, 12000.0, "Reguler")
)

val dummyItems = mutableStateListOf(
    ItemPesanan(produkDummyList[0], 2),
    ItemPesanan(produkDummyList[1], 1),
    ItemPesanan(produkDummyList[2], 3)
)

val dummyPesanan = mutableStateListOf(
    Pesanan(
        id = 1,
        user = dummyUsers["user1@example.com"]!!,
        items = mutableStateListOf(dummyItems[0], dummyItems[1]),
        status = StatusPesanan.DIPROSES,
        ekspedisi = ekspedisiDummy[0],
        tanggal = LocalDate.of(2025, 10, 16),
        metodePembayaran = MetodePembayaran.TRANSFER_BANK
    ),
    Pesanan(
        id = 2,
        user = dummyUsers["user2@example.com"]!!,
        items = mutableStateListOf(dummyItems[2]),
        status = StatusPesanan.DIKIRIM,
        ekspedisi = ekspedisiDummy[1],
        tanggal = LocalDate.of(2025, 10, 17),
        metodePembayaran = MetodePembayaran.GOPAY
    ),
    Pesanan(
        id = 3,
        user = dummyUsers["user3@example.com"]!!,
        items = mutableStateListOf(dummyItems[1], dummyItems[2]),
        status = StatusPesanan.SELESAI,
        ekspedisi = ekspedisiDummy[2],
        tanggal = LocalDate.of(2025, 10, 17),
        metodePembayaran = MetodePembayaran.OVO
    ),
    Pesanan(
        id = 4,
        user = dummyUsers["user1@example.com"]!!,
        items = mutableStateListOf(dummyItems[0], dummyItems[2]),
        status = StatusPesanan.DIBATALKAN,
        ekspedisi = ekspedisiDummy[0],
        tanggal = LocalDate.of(2025, 10, 13),
        metodePembayaran = MetodePembayaran.CASH
    )
)

val ulasanList: MutableList<Ulasan> = mutableStateListOf(
    // =========================================================================
    // PRODUK ID 1: Sepeda Gunung (5 Ulasan)
    // =========================================================================
    Ulasan(1, 1, 1, 5f, "Kualitas mantap, pengiriman cepat! Sepedanya ringan dan enak buat nanjak.", LocalDate.now().minusDays(5)),
    Ulasan(2, 1, 2, 4.5f, "Produk sesuai deskripsi, recommended. Hanya saja rem perlu disetel sedikit.", LocalDate.now().minusDays(3)),
    Ulasan(7, 1, 3, 4f, "Lumayan bagus dengan harga segini. Pengemasan rapi.", LocalDate.now().minusDays(10)),
    Ulasan(8, 1, 1, 4.5f, "Suspensi bekerja dengan baik, puas beli di sini.", LocalDate.now().minusDays(1)),
    Ulasan(17, 1, 2, 5f, "Sepeda sangat kokoh. Pengalaman bersepeda jadi jauh lebih nyaman. Lima bintang!", LocalDate.now().minusDays(2)),

    // =========================================================================
    // PRODUK ID 2: Helm Sepeda (6 Ulasan)
    // =========================================================================
    Ulasan(3, 2, 3, 4f, "Helmnya oke, tapi warna agak beda dari foto. Fitur dial-nya berfungsi.", LocalDate.now().minusDays(7)),
    Ulasan(4, 2, 2, 3.5f, "Cukup nyaman dipakai. Ventilasi bagus, tidak terlalu panas.", LocalDate.now().minusDays(2)),
    Ulasan(9, 2, 1, 5f, "Helm ringan banget! Cocok buat lari dan gowes cepat. Terbaik!", LocalDate.now().minusDays(4)),
    Ulasan(10, 2, 3, 5f, "Modelnya futuristik dan paddingnya empuk. Suka!", LocalDate.now()),
    Ulasan(18, 2, 2, 4.5f, "Proteksi terlihat solid. Pengiriman cepat dan aman. Highly recommended.", LocalDate.now().minusDays(1)),
    Ulasan(19, 2, 1, 3f, "Agak sempit di kepala, harusnya ada pilihan ukuran.", LocalDate.now().minusDays(6)),

    // =========================================================================
    // PRODUK ID 3: Sarung Tangan (5 Ulasan)
    // =========================================================================
    Ulasan(5, 3, 1, 5f, "Bahannya bagus, tidak panas, bantalan gelnya pas untuk mengurangi pegal di tangan.", LocalDate.now().minusDays(1)),
    Ulasan(6, 3, 2, 4.2f, "Suka banget, kualitas top! Velcro-nya kuat dan tidak mudah lepas.", LocalDate.now()),
    Ulasan(11, 3, 3, 4.8f, "Pengiriman cepat. Sarung tangan pas di ukuran tangan saya.", LocalDate.now().minusDays(15)),
    Ulasan(12, 3, 2, 4f, "Warna sesuai, harga terjangkau. Good job UMKM!", LocalDate.now().minusDays(9)),
    Ulasan(20, 3, 1, 5f, "Sangat pas dan nyaman, sangat berguna untuk gowes panjang.", LocalDate.now().minusDays(3)),

    // =========================================================================
    // PRODUK ID 4: Botol Minum (4 Ulasan)
    // =========================================================================
    Ulasan(13, 4, 1, 5f, "Anti bocor total! Sempurna untuk holder sepeda. Praktis dan ringan.", LocalDate.now().minusDays(2)),
    Ulasan(14, 4, 3, 4.7f, "Bahan tebal dan kuat, nozzle-nya nyaman. Cepat sampai.", LocalDate.now().minusDays(4)),
    Ulasan(21, 4, 2, 4f, "Warna cerah, terlihat sporty. Cuma agak sulit dicuci bagian dalamnya.", LocalDate.now().minusDays(7)),
    Ulasan(22, 4, 3, 5f, "Botol minum paling tahan banting yang pernah saya punya. Mantap!", LocalDate.now().minusDays(1)),

    // =========================================================================
    // PRODUK ID 5: Lampu Sepeda (4 Ulasan)
    // =========================================================================
    Ulasan(15, 5, 2, 4.5f, "Cahaya sangat terang, cocok untuk gowes subuh. Pemasangan mudah.", LocalDate.now().minusDays(6)),
    Ulasan(16, 5, 1, 4f, "Pengisian cepat via USB. Barang sampai dengan aman.", LocalDate.now().minusDays(3)),
    Ulasan(23, 5, 3, 5f, "Lampu paling terang di kelasnya! Waterproof berfungsi baik saat hujan ringan.", LocalDate.now()),
    Ulasan(24, 5, 2, 4.5f, "Desain keren dan fitur kedipnya sangat membantu di jalan raya.", LocalDate.now().minusDays(1))
)