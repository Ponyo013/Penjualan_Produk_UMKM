package com.example.penjualan_produk_umkm.utils

import android.content.Context
import android.widget.Toast
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.google.firebase.firestore.FirebaseFirestore

object DataSeeder {

    private val db = FirebaseFirestore.getInstance()

    // --- TEMPAT PASTE URL IMAGEKIT DI SINI ---
    // Pastikan Anda sudah mengganti string kosong "" dengan URL gambar dari ImageKit Anda
    private const val IMG_SYNCLINE = "https://ik.imagekit.io/ngj1vwwr8/produk/503446.jpg"
    private const val IMG_RELIC = "https://ik.imagekit.io/ngj1vwwr8/produk/502661.jpg"
    private const val IMG_SISKIU_D6 = "https://ik.imagekit.io/ngj1vwwr8/produk/pa00715.jpg"
    private const val IMG_SISKIU_DSE = "https://ik.imagekit.io/ngj1vwwr8/produk/502873.jpg"
    private const val IMG_CLAIRE = "https://ik.imagekit.io/ngj1vwwr8/produk/a59c9f1e-3ad4-47ec-94a3-c354a42a6e2f.jpg"
    private const val IMG_OOSTEN = "https://ik.imagekit.io/ngj1vwwr8/produk/502611.jpg"
    private const val IMG_RAZOR = "https://ik.imagekit.io/ngj1vwwr8/produk/503320_1.jpg"
    private const val IMG_TERN_VERGE = "https://ik.imagekit.io/ngj1vwwr8/produk/732946.jpg"
    private const val IMG_TERN_LINK = "https://ik.imagekit.io/ngj1vwwr8/produk/731228.jpg"
    private const val IMG_TRAVIS = "https://ik.imagekit.io/ngj1vwwr8/produk/502996.jpg"

    private const val IMG_PEDAL_XTR = "https://ik.imagekit.io/ngj1vwwr8/produk/740352001.jpg"
    private const val IMG_BARTAPE_02 = "https://ik.imagekit.io/ngj1vwwr8/produk/741354001.jpg"
    private const val IMG_BARTAPE_01 = "https://ik.imagekit.io/ngj1vwwr8/produk/download.jpg"
    private const val IMG_GRX_BRAKE = "https://ik.imagekit.io/ngj1vwwr8/produk/pa01788.jpg"
    private const val IMG_BB_RS501 = "https://ik.imagekit.io/ngj1vwwr8/produk/736891.jpg"
    private const val IMG_CEMA_BB = "https://ik.imagekit.io/ngj1vwwr8/produk/BB86-BB92%20for%20SRAM%20DUB%20-%20INTERLOCK.jpg"
    private const val IMG_CEMA_PULLEY = "https://ik.imagekit.io/ngj1vwwr8/produk/740231.jpg"
    private const val IMG_KABEL_SHIFT = "https://ik.imagekit.io/ngj1vwwr8/produk/737777.jpg"
    private const val IMG_BROOKS_C17 = "https://ik.imagekit.io/ngj1vwwr8/produk/731545001.jpg"
    private const val IMG_PRO_STEM = "https://ik.imagekit.io/ngj1vwwr8/produk/download.jpg"
    private const val IMG_FD_105 = "https://ik.imagekit.io/ngj1vwwr8/produk/737483001.jpg"

    private const val IMG_BOTOL_500 = "https://ik.imagekit.io/ngj1vwwr8/produk/736335001.jpg?updatedAt=1765013217825"
    private const val IMG_BEL_RING = "https://ik.imagekit.io/ngj1vwwr8/produk/734472001.jpg?updatedAt=1765011255916"
    private const val IMG_BRACKET_REFLECTOR = "https://ik.imagekit.io/ngj1vwwr8/produk/200212.jpg"
    private const val IMG_KICKSTAND_26 = "https://ik.imagekit.io/ngj1vwwr8/produk/731845001.jpg"
    private const val IMG_TOOL_CRANK = "https://ik.imagekit.io/ngj1vwwr8/produk/700218.jpg"
    private const val IMG_BAN_DALAM_KENDA = "https://ik.imagekit.io/ngj1vwwr8/produk/PA00298.jpg"
    private const val IMG_BOTTLE_HOLDER_POLISPORT = "https://ik.imagekit.io/ngj1vwwr8/produk/polisport_polisport_basic_bottle_cage_-_cagak_botol_sepeda_full08_pgeb0wcz.webp"
    private const val IMG_BRACKET_CATEYE = "https://ik.imagekit.io/ngj1vwwr8/produk/CA5342440_1823.webp"
    private const val IMG_BOTOL_LIVE_580 = "https://ik.imagekit.io/ngj1vwwr8/produk/download.jpg"
    private const val IMG_BEL_TWIST = "https://ik.imagekit.io/ngj1vwwr8/produk/736829001.jpg"
    private const val IMG_POMPA_MP01 = "https://ik.imagekit.io/ngj1vwwr8/produk/738465_2.jpg"
    private const val IMG_TOPEAK_NINJA = "https://ik.imagekit.io/ngj1vwwr8/produk/733663001.jpg?updatedAt=1765010916730"
    private const val IMG_KACAMATA_ALTALIST = "https://ik.imagekit.io/ngj1vwwr8/produk/pa01597.jpg"
    private const val IMG_HELM_ENTITY = "https://ik.imagekit.io/ngj1vwwr8/produk/726109.jpg"
    private const val IMG_GLOVE_ARELI = "https://ik.imagekit.io/ngj1vwwr8/produk/729886.jpg"
    private const val IMG_LEG_SLEEVE = "https://ik.imagekit.io/ngj1vwwr8/produk/727979.jpg"
    // --- KATEGORI: PERAWATAN (NEW) ---
    private const val IMG_FL_DRY_TEFLON = ""
    private const val IMG_FL_WET_LUBE = ""
    private const val IMG_SCHWALBE_CEMENT = ""
    private const val IMG_FL_KRYTECH = ""
    private const val IMG_FL_CERAMIC_WET = ""
    private const val IMG_FL_CERAMIC_WAX = ""
    private const val IMG_FL_NO_DRIP = ""
    private const val IMG_FL_1STEP = ""
    private const val IMG_FL_FIBER_GRIP = ""
    // URL Default jika gambar belum tersedia (Placeholder)
    private const val IMG_DEFAULT = "https://ik.imagekit.io/demo/img/image4.jpeg"
    // -----------------------------------------

    fun seedProducts(context: Context) {
        // Gunakan versi v5 agar seeding berjalan ulang jika sebelumnya v4/v3 sudah ada
        val prefs = context.getSharedPreferences("seeder_prefs", Context.MODE_PRIVATE)
        val isSeeded = prefs.getBoolean("is_products_seeded_v5", false)

        if (isSeeded) {
            return // Stop jika sudah seeded versi ini
        }

        Toast.makeText(context, "Mengupdate Data Produk...", Toast.LENGTH_SHORT).show()

        val batch = db.batch()

        val produkList = listOf(
            // --- KATEGORI: SEPEDA ---
            createProduct("Polygon Sepeda MTB Dual Suspensi Syncline DR8", 50000000.0, 3, "Sepeda", IMG_SYNCLINE, 5.0f, 102, "Polygon Syncline DR8 adalah monster trail sejati. Didesain dengan geometri modern untuk kontrol maksimal di medan teknikal, ketahanan luar biasa, dan kenyamanan tanpa kompromi. Sepeda MTB ini siap menemani Anda menaklukkan jalur off-road berbatu hingga rute cross-country (XC) yang menuntut kecepatan tinggi.", "Frame: ACX XC Performance Carbon\nFork: Fox 32 Rhythm 100mm\nDrivetrain: Shimano Deore XT 1x12 Speed\nBrakes: Shimano Hydraulic Disc\nWheel: Entity XL2 Tubeless Ready"),
            createProduct("Polygon Sepeda MTB Junior Relic 24 2021", 2900000.0, 15, "Sepeda", IMG_RELIC, 4.8f, 24, "Hadiah terbaik untuk petualang muda! Polygon Relic 24 dirancang khusus untuk anak-anak yang siap beralih ke sepeda gunung sungguhan. Frame AL6 Alloy yang ringan namun kokoh serta geometri yang disesuaikan untuk postur anak, memberikan kepercayaan diri lebih saat melibas jalanan.", "Frame: AL6 Rigid Frame\nFork: Suspension Fork 60mm\nShifter: Shimano Tourney 1x8 Speed\nBrakes: Mechanical Disc Brake\nWheel: 24 Inch"),
            createProduct("Polygon Sepeda MTB Dual Suspensi Siskiu D6", 14500000.0, 8, "Sepeda", IMG_SISKIU_D6, 4.9f, 67, "Siskiu D6 adalah definisi sepeda trail all-rounder dengan value terbaik. Sistem suspensi one-piece linkage mengurangi flex lateral, memberikan handling presisi. Dilengkapi Dropper Seatpost bawaan untuk penyesuaian tinggi sadel instan saat turunan.", "Frame: ALX XC Trail Frame\nFork: SR Suntour XCR 32 Air 120mm\nRear Shock: X-Fusion O2 Pro RL\nDrivetrain: Shimano Deore 1x10 Speed\nSeatpost: TranzX Dropper"),
            createProduct("Polygon Sepeda MTB Dual Suspensi Siskiu DSE", 16500000.0, 5, "Sepeda", IMG_SISKIU_DSE, 5.0f, 82, "Edisi Spesial Siskiu DSE menghadirkan peningkatan performa untuk rider agresif. Responsivitas tinggi di tikungan tajam dan stabilitas saat kecepatan tinggi. Warna eksklusif dan finishing premium membuatnya tampil beda di lintasan.", "Frame: ALX Trail Suspension\nFork: RockShox Recon Silver RL 120mm\nRear Shock: RockShox Deluxe Select+\nDrivetrain: Shimano Deore 1x11 Speed\nBrakes: Shimano Hydraulic Disc"),
            createProduct("Polygon Sepeda Kota Claire 24", 3500000.0, 12, "Sepeda", IMG_CLAIRE, 4.7f, 30, "City bike bergaya Eropa yang chic. Frame step-through rendah memudahkan naik-turun. Dilengkapi keranjang depan fungsional dan boncengan belakang kokoh. Pilihan sempurna untuk gaya hidup aktif dan stylish di perkotaan.", "Frame: AL6 City Frame\nFork: Hi-Ten Steel Rigid\nDrivetrain: Shimano Tourney 7 Speed\nBrakes: V-Brake Alloy\nFeature: Basket & Carrier"),
            createProduct("Polygon Sepeda Kota Oosten 26 2020", 3950000.0, 10, "Sepeda", IMG_OOSTEN, 4.6f, 51, "Klasik dan nyaman. Polygon Oosten 26 mengembalikan nostalgia dengan sentuhan modern. Posisi berkendara tegak (upright) sangat ramah punggung. Sadel lebar dengan per ganda menjamin kenyamanan ekstra untuk mobilitas harian.", "Frame: AL6 City Alloy\nDrivetrain: Shimano Altus 8 Speed\nBrakes: V-Brake\nSaddle: Selle Royal Ondina\nWheel: 26 Inch"),
            createProduct("Polygon Sepeda BMX Razor Micro", 3250000.0, 20, "Sepeda", IMG_RAZOR, 4.8f, 40, "Langkah awal menuju podium BMX! Sepeda race BMX ukuran kecil khusus anak-anak. Frame ultra-ringan dan kaku memaksimalkan transfer tenaga, memberikan akselerasi eksplosif saat start.", "Frame: AL6 BMX Race Frame\nFork: Hi-Ten Steel\nCrankset: Alloy 130mm 36T\nTire: Kenda Kompact\nTarget: Junior Racer"),
            createProduct("Tern Sepeda Lipat Verge P10", 11000000.0, 6, "Sepeda", IMG_TERN_VERGE, 4.9f, 10, "Speed demon dalam wujud sepeda lipat. Dengan roda 451 (22 inch) yang lebih besar, sepeda ini menawarkan kestabilan setara sepeda full-size. Teknologi lipatan T-Tuned memberikan kekakuan frame luar biasa.", "Frame: Tern Verge Aluminum\nWheel: 22\" (451)\nDrivetrain: Shimano Deore 1x10 Speed\nBrakes: Shimano Hydraulic Disc\nFolded Size: 38x80x74 cm"),
            createProduct("Tern Sepeda Lipat Link D8", 5400000.0, 8, "Sepeda", IMG_TERN_LINK, 4.7f, 25, "Sahabat commuter urban. Kombinasi sempurna harga, performa, dan portabilitas. Andros Stem unik memungkinkan pengaturan posisi stang instan. Ban Schwalbe Big Apple memberikan suspensi alami melibas jalanan kota.", "Frame: Tern Link Aluminum\nDrivetrain: Shimano Claris 1x8 Speed\nStem: Tern Andros Adjustable\nTire: Schwalbe Big Apple\nFolded Size: 38x79x72 cm"),
            createProduct("Polygon Sepeda BMX Travis", 1700000.0, 25, "Sepeda", IMG_TRAVIS, 4.5f, 55, "BMX freestyle untuk pemula yang ingin tampil keren. Dibuat tangguh untuk menahan benturan saat belajar trik dasar. Dilengkapi rotor stang 360 derajat tanpa melilit kabel rem.", "Frame: Hi-Ten Steel Freestyle\nFork: Hi-Ten Steel\nBrakes: Alloy V-Brake w/ Rotor\nWheel: 20 Inch\nDrivetrain: Single Speed"),

            // --- KATEGORI: SPARE PARTS ---
            createProduct("Shimano Pedal Sepeda XTR XC IPD-M9200", 2498000.0, 10, "Spare Parts", IMG_PEDAL_XTR, 5.0f, 8, "Pedal balap XC terbaik dari Shimano. XTR M9200 menawarkan transfer tenaga yang tak tertandingi dengan bobot yang sangat ringan. Desain body yang ramping meningkatkan clearance saat melewati bebatuan.", "Model: PD-M9200\nType: SPD (Off-Road)\nBody Material: Aluminum Anodized\nAxle: Chromoly Steel\nWeight: Approx 310g/pair"),
            createProduct("Shimano Rem Hidrolik Sepeda GRX I-RX820-D 2x12 Speed", 4248000.0, 5, "Spare Parts", IMG_GRX_BRAKE, 5.0f, 3, "Upgrade performa gravel bike Anda ke level tertinggi. Shimano GRX RX820 dirancang khusus untuk medan gravel yang kasar. Tuas rem yang ergonomis memberikan kontrol presisi dan daya pengereman yang pakem.", "Series: GRX 12-speed\nPosition: Right / Rear (Hydraulic)\nMount: Flat Mount\nLever Type: Dual Control Lever"),
            createProduct("Shimano Bottom Bracket Sepeda EBB-RS501-B BSA", 208000.0, 30, "Spare Parts", IMG_BB_RS501, 4.8f, 60, "Bottom Bracket standar Shimano yang handal dan tahan lama. Menggantikan seri RS500, RS501 hadir dengan seal yang lebih baik untuk mencegah masuknya air dan debu, menjaga putaran crank tetap loncer lebih lama.", "Type: BSA (Threaded/Drat)\nShell Width: 68mm\nAxle Diameter: 24mm (Hollowtech II)\nMaterial: Aluminum Cups"),
            createProduct("Cema Bottom Bracket Sepeda BB86/92x24 Interlock", 2498000.0, 4, "Spare Parts", IMG_CEMA_BB, 4.9f, 5, "Solusi premium untuk menghilangkan bunyi 'creaking' pada frame Press-Fit. Teknologi Interlock dari Cema menyatukan kedua cup BB di dalam frame, menciptakan struktur yang kaku dan presisi.", "Type: Interlock Press-Fit\nFit for: BB86 / BB92 Frames\nAxle: 24mm (Shimano Compatible)\nBearing: SRC Ceramic"),
            createProduct("Cema Pulley Set Aluminium Ceramic 12x14T", 2398000.0, 6, "Spare Parts", IMG_CEMA_PULLEY, 5.0f, 4, "Upgrade 'Free Speed' untuk RD Anda. Pulley wheel aluminium dengan bearing keramik dari Cema ini mengurangi gesekan rantai secara drastis dibandingkan pulley standar.", "Material: 7075 T6 Aluminium Alloy\nBearing: Full Ceramic\nTeeth: 12T (Upper) / 14T (Lower)\nCompatibility: Shimano 12 Speed"),
            createProduct("Shimano Kabel Inner Shift Dura-Ace XTR Polymer", 168000.0, 50, "Spare Parts", IMG_KABEL_SHIFT, 4.7f, 100, "Kunci dari perpindahan gigi yang ringan dan presisi. Kabel inner shifter Polymer Coating mengurangi gesekan antara kabel dan housing hingga ke titik minimum.", "Model: Y63Z98950\nCoating: Polymer Coated\nDiameter: 1.2mm\nLength: 2100mm\nMaterial: Stainless Steel"),
            createProduct("Pro Handle Stem Sepeda Vibe 31.8 mm -10 Deg", 1798000.0, 8, "Spare Parts", IMG_PRO_STEM, 4.8f, 12, "Pilihan pro peloton untuk kekakuan dan aerodinamika. Stem PRO Vibe didesain untuk integrasi penuh dengan kabel Di2, memberikan tampilan kokpit yang bersih (clean look).", "Material: Anodized 7075 Alloy\nAngle: -10 Degree\nClamp Diameter: 31.8mm\nSteerer: 1-1/8 Inch"),
            createProduct("Shimano Front Derailleur 105 IFD-R7100-F 2x12", 448000.0, 15, "Spare Parts", IMG_FD_105, 4.7f, 20, "Shifting depan yang ringan dan intuitif untuk era 12-speed mekanikal. FD-R7100 menghadirkan teknologi toggle link yang memberikan respon perpindahan gigi yang cepat dan membutuhkan tenaga tangan yang lebih sedikit.", "Series: Shimano 105 R7100\nSpeed: 2x12 Speed\nMount: Brazed-On\nChain Line: 44.5mm\nMax Chainring: 50-52T"),
            createProduct("Kenda Ban Dalam Sepeda MTB", 59000.0, 80, "Spare Parts", IMG_BAN_DALAM_KENDA, 4.7f, 150, "Ban dalam andalan para rider MTB. Kenda dikenal dengan karet butyl berkualitas tinggi yang elastis dan tahan lama. Ketebalan dinding yang merata meminimalkan risiko bocor halus.", "Ukuran: 27.5 x 1.90 - 2.125 (Universal MTB)\nMaterial: Mold Cured Butyl Rubber\nValve: Presta (Pentil Kecil)"),

            // --- KATEGORI: AKSESORIS ---
            createProduct("Cycliste Bar Tape Sepeda BTR-02 Reflective", 148000.0, 25, "Aksesoris", IMG_BARTAPE_02, 4.6f, 40, "Tingkatkan kenyamanan dan keamanan gowes malam Anda. Bar tape Cycliste BTR-02 dilengkapi lapisan reflektif yang memantulkan cahaya saat terkena sorot lampu kendaraan.", "Material: PU + EVA Foam\nThickness: 3.0mm\nFeature: Reflective Pattern\nInclude: Bar Plugs & Finishing Tape"),
            createProduct("Cycliste Bar Tape Sepeda BTR-01 Reflective", 148000.0, 20, "Aksesoris", IMG_BARTAPE_01, 4.5f, 35, "Gaya dan fungsi dalam satu balutan. Seri BTR-01 menawarkan pola geometris unik yang tidak hanya estetik tetapi juga anti-slip. Material EVA foam berkualitas tinggi menyerap keringat.", "Material: High Density EVA\nThickness: 2.5mm\nLength: 2150mm\nFeature: Anti-slip & Shock Absorbing"),
            createProduct("Brooks Sadel Sepeda C17 Special", 2998000.0, 5, "Aksesoris", IMG_BROOKS_C17, 4.9f, 7, "Kenyamanan legendaris tanpa masa break-in. Brooks Cambium C17 dibuat dari karet alam vulkanisir dan lapisan katun organik yang fleksibel, mengikuti gerak tubuh pengendara.", "Material: Vulcanised Natural Rubber, Organic Cotton\nFrame: Steel with Copper Plated Backplate\nDimensions: L 283 x W 164 x H 52 mm"),
            createProduct("Polygon Botol Minum Sepeda Cycling 500 ml", 17000.0, 100, "Aksesoris", IMG_BOTOL_500, 4.5f, 200, "Jaga hidrasi Anda tetap optimal saat bersepeda dengan Botol Minum Polygon. Dibuat dari material plastik berkualitas food-grade yang aman dan bebas BPA.", "Kapasitas: 500 ml\nMaterial: LDPE Plastic (BPA Free)\nDiameter: Standar Bottle Cage\nFitur: Easy Squeeze"),
            createProduct("Polygon Bel Sepeda Ring", 19000.0, 50, "Aksesoris", IMG_BEL_RING, 4.6f, 85, "Tingkatkan keselamatan di jalan raya dengan Polygon Bell Ring. Menghasilkan suara 'ting' yang nyaring dan jernih untuk memberi peringatan kepada pejalan kaki.", "Tipe: Classic Ring Bell\nMaterial: Alloy Dome, Plastic Base\nMount: Handlebar Clamp (Universal)\nSuara: Jernih & Nyaring"),
            createProduct("Polygon Bracket Reflector Sepeda B-12 Rear", 3000.0, 200, "Aksesoris", IMG_BRACKET_REFLECTOR, 4.8f, 50, "Bracket pengganti untuk reflektor belakang sepeda Anda. Komponen kecil namun vital untuk memastikan reflektor terpasang kokoh di seatpost.", "Model: B-12\nPosisi: Belakang (Rear)\nKompatibilitas: Reflektor Standar Polygon\nMaterial: Durable Plastic"),
            createProduct("Polygon Kickstand Sepeda 26\" Clamp on Chainstay", 29500.0, 30, "Aksesoris", IMG_KICKSTAND_26, 4.4f, 60, "Standar samping kokoh khusus untuk sepeda ukuran roda 26 inci. Dipasang pada bagian chainstay frame, memberikan kestabilan saat sepeda diparkir.", "Kompatibilitas: Sepeda Roda 26 Inch\nTipe Mount: Clamp on Chainstay\nMaterial: Steel dengan Ujung Karet Anti-slip"),
            createProduct("Polisport Tempat Botol Minum Sepeda Basic", 49000.0, 40, "Aksesoris", IMG_BOTTLE_HOLDER_POLISPORT, 4.5f, 45, "Bottle cage ringan dan tangguh buatan Polisport. Desain geometrisnya memegang botol dengan kencang namun tetap memudahkan akses ambil-taruh botol saat berkendara.", "Material: Polycarbonate High Resistance\nBerat: ~35 gram\nKompatibilitas: Botol diameter standar (500-750ml)"),
            createProduct("CatEye CFB-100 Center Fork Bracket", 49000.0, 10, "Aksesoris", IMG_BRACKET_CATEYE, 4.8f, 12, "Solusi cerdas untuk memasang lampu depan CatEye. Bracket ini memungkinkan Anda memindahkan posisi lampu dari stang ke bagian fork.", "Model: CFB-100\nPosisi Pasang: Lubang rem depan (Fork Center)\nKompatibilitas: Hampir semua lampu depan CatEye"),
            createProduct("Polygon Botol Minum Sepeda Live 580 ml", 59000.0, 60, "Aksesoris", IMG_BOTOL_LIVE_580, 4.6f, 90, "Seri 'Live' dari Polygon hadir dengan kapasitas sedikit lebih besar (580ml) untuk gowes jarak menengah. Material plastik fleksibel memudahkan pemerasan air (squeezable).", "Kapasitas: 580 ml\nFitur: Dust Cover (Penutup Debu)\nMaterial: PP5 (Food Grade)\nWarna: Transparan"),
            createProduct("Polygon Bel Sepeda Twist", 63200.0, 35, "Aksesoris", IMG_BEL_TWIST, 4.7f, 40, "Bel sepeda dengan mekanisme putar yang unik. Cukup putar *dome* bel untuk membunyikan suara yang nyaring. Desainnya yang *low-profile* membuat tampilan kokpit sepeda tetap rapi.", "Mekanisme: Rotary / Twist Action\nSuara: Single Ping yang panjang\nMaterial: Alloy & Composite Base\nDiameter Clamp: 22.2mm"),
            createProduct("Topeak Tempat Botol Minum Sepeda Ninja Master+ Cage X", 99000.0, 15, "Aksesoris", IMG_TOPEAK_NINJA, 4.9f, 30, "Lebih dari sekadar tempat botol. Topeak Ninja Master+ Cage X adalah sistem modular cerdas. Anda dapat menambahkan berbagai tools Ninja di bagian bawah cage ini.", "Model: TNJC-X\nMaterial: Engineering Grade Polymer\nFitur: Ninja Master+ QuickClick Mount System\nBerat: 48g"),
            createProduct("Altalist Kacamata Sepeda Kaku SP 2 Photochromic", 648000.0, 8, "Aksesoris", IMG_KACAMATA_ALTALIST, 5.0f, 15, "Penglihatan jernih di segala kondisi cahaya. Lensa Photochromic pintar otomatis menggelap saat terik matahari dan menjernih saat mendung atau malam hari.", "Lensa: Photochromic (Transisi Otomatis)\nProteksi: UV400 Protection\nFrame: TR90 (Fleksibel & Kuat)\nFitur: Adjustable Nose Pad"),
            createProduct("Entity Helm Sepeda Mountain MH15", 548000.0, 12, "Aksesoris", IMG_HELM_ENTITY, 4.8f, 25, "Proteksi maksimal untuk petualangan gunung Anda. Entity MH15 dibuat dengan teknologi In-Mold yang menyatukan shell luar dan busa EPS menjadi satu kesatuan yang kokoh.", "Konstruksi: In-Mold Technology\nSertifikasi: EN1078 (Standar Eropa)\nFitur: Dial Fit System (Pengatur Ukuran), Visor Lepas-Pasang"),
            createProduct("Polygon Sarung Tangan Sepeda AM Areli", 149000.0, 30, "Aksesoris", IMG_GLOVE_ARELI, 4.6f, 55, "Sarung tangan All-Mountain (AM) full finger untuk perlindungan total. Bagian telapak dilengkapi padding strategis untuk meredam getaran stang.", "Tipe: Full Finger Glove\nMaterial: Synthetic Leather, Mesh, Spandex\nFitur: Touchscreen Compatible, Silicone Grip"),
            createProduct("Polygon Leg Sleeve Sepeda Felic", 99000.0, 40, "Aksesoris", IMG_LEG_SLEEVE, 4.5f, 70, "Lindungi kaki Anda dari sengatan matahari dan goresan ringan. Polygon Felic Leg Sleeve terbuat dari bahan Lycra elastis yang memberikan efek kompresi ringan.", "Material: Lycra / Spandex Blend\nFitur: UV Protection (UPF 50+), Moisture Wicking\nUkuran: S, M, L, XL"),

            // --- KATEGORI: PERAWATAN ---
            createProduct(
                "Finish Line Pelumas Rantai Sepeda Teflon Plus Dry",
                148000.0, 50, "Perawatan", IMG_FL_DRY_TEFLON, 4.9f, 200,
                "Pelumas rantai 'Kering' legendaris dengan teknologi Teflon™ fluoropolymer. Cairan ini masuk ke sela-sela rantai lalu mengering menjadi lapisan lilin (wax) yang licin namun tidak menarik debu atau pasir. Sangat ideal untuk kondisi jalur yang kering, berdebu, dan panas.",
                "Volume: 120ml (4oz)\nType: Dry Lube (Wax-like)\nTech: Teflon™ Fluoropolymer\nBest for: Dry & Dusty Conditions"
            ),
            createProduct(
                "Finish Line Pelumas Sepeda Wet Lube",
                228000.0, 40, "Perawatan", IMG_FL_WET_LUBE, 4.8f, 150,
                "Pelumas sintetis viskositas tinggi untuk kondisi ekstrem. Finish Line Wet Lube dirancang untuk bertahan lama dalam kondisi basah, berlumpur, dan hujan lebat. Formulanya menolak air (water-repellent) dan mencegah karat, memastikan drivetrain tetap senyap dan halus saat menerjang genangan.",
                "Volume: 240ml (8oz) Aerosol\nType: Wet Lube (Synthetic Oil)\nFeature: Water Resistant, Anti-Rust\nBest for: Wet, Muddy, Snowy Conditions"
            ),
            createProduct(
                "Schwalbe Tubular Cement Tube Sepeda",
                49000.0, 20, "Perawatan", IMG_SCHWALBE_CEMENT, 4.7f, 30,
                "Lem khusus untuk memasang ban tubular pada rim (velg). Diformulasikan khusus oleh Schwalbe untuk daya rekat maksimal pada rim karbon maupun aluminium. Cepat kering dan tahan panas, menjaga ban tetap menempel erat saat cornering tajam.",
                "Volume: 30g (Cukup untuk 2 ban)\nApplication: Carbon & Alloy Rims\nType: Contact Cement\nMade in: Germany"
            ),
            createProduct(
                "Finish Line Pelumas Sepeda Krytech Wax Lube",
                99000.0, 35, "Perawatan", IMG_FL_KRYTECH, 4.6f, 60,
                "Pelumas wax paling bersih dari Finish Line. Menggunakan formula Krytox® dari DuPont™ yang membentuk lapisan lilin keras dan kering. Rantai akan tetap bersih berkilau tanpa residu minyak hitam. Cocok untuk goweser yang memprioritaskan kebersihan drivetrain.",
                "Volume: 60ml (2oz)\nType: Wax Lube\nTech: DuPont™ Krytox®\nBenefit: No oily residue, Super clean"
            ),
            createProduct(
                "Finish Line Pelumas Sepeda Ceramic Wet Lube",
                108000.0, 25, "Perawatan", IMG_FL_CERAMIC_WET, 4.9f, 45,
                "Pelumas balap paling canggih dari Finish Line. Mengandung partikel Nano-Ceramic Boron Nitride yang melapisi gesekan logam. Semakin sering dipakai, semakin licin karena partikel keramiknya memoles permukaan rantai. Tahan air namun tidak se-lengket wet lube biasa.",
                "Volume: 60ml (2oz)\nType: Ceramic Wet Lube\nTech: Nano-Ceramic Boron Nitride\nBenefit: Ultra low friction, Long lasting"
            ),
            createProduct(
                "Finish Line Pelumas Sepeda Ceramic Wax Lube",
                108000.0, 30, "Perawatan", IMG_FL_CERAMIC_WAX, 4.8f, 55,
                "Kombinasi kebersihan Wax Lube dengan performa Keramik. Pelumas ini mengering sepenuhnya namun meninggalkan lapisan keramik mikroskopis yang sangat licin. Mengurangi suara berisik rantai secara signifikan dan memperpanjang umur pakai komponen drivetrain.",
                "Volume: 60ml (2oz)\nType: Ceramic Wax Lube\nTech: Nano-Ceramic Platelets\nBenefit: Silent running, Cleanliness"
            ),
            createProduct(
                "Finish Line Botol Pelumas Sepeda No Drip Chain Luber",
                138000.0, 15, "Perawatan", IMG_FL_NO_DRIP, 4.5f, 25,
                "Alat aplikator pelumas cerdas agar tidak boros dan berantakan. Botol ini dilengkapi bantalan busa khusus di ujungnya yang mengoleskan pelumas secara merata ke setiap mata rantai tanpa menetes ke lantai atau velg. Hemat pelumas hingga 50%.",
                "Include: 1x Botol Kosong, 2x Busa Pad, 1x Travel Cap\nFungsi: Aplikasi pelumas presisi\nKompatibel: Semua jenis pelumas cair"
            ),
            createProduct(
                "Finish Line Pelumas Sepeda 1-Step Cleaner & Lubricant",
                248000.0, 20, "Perawatan", IMG_FL_1STEP, 4.7f, 80,
                "Solusi praktis untuk perawatan cepat. Produk 2-in-1 yang membersihkan kotoran lama sekaligus memberikan pelumasan baru dalam satu langkah. Cukup semprot dan lap. Sempurna untuk sepeda komuter atau perawatan ringan di tengah perjalanan.",
                "Volume: 360ml (12oz) Aerosol\nType: Cleaner + Lube\nFormula: Anti-corrosion agents\nBest for: Commuters, Quick maintenance"
            ),
            createProduct(
                "Finish Line Pelumas Sepeda Fiber Grip",
                168000.0, 10, "Perawatan", IMG_FL_FIBER_GRIP, 4.9f, 15,
                "Pasta khusus (Assembly Gel) untuk komponen Carbon Fiber. Fiber Grip menciptakan gesekan tambahan pada permukaan karbon (seatpost, stang, stem) sehingga tidak perlu dikencangkan berlebihan yang bisa merusak frame. Wajib punya untuk pemilik sepeda karbon.",
                "Volume: 50g (1.75oz)\nFungsi: Meningkatkan friksi/grip\nAplikasi: Seatpost karbon, Stang karbon\nBenefit: Mencegah slip & over-tightening"
            )
        )

        produkList.forEach { produk ->
            val docRef = db.collection("produk").document(produk.id)
            batch.set(docRef, produk)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "Seeding Sukses! (v5)", Toast.LENGTH_LONG).show()
                prefs.edit().putBoolean("is_products_seeded_v5", true).apply()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Seeding Gagal: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun createProduct(
        nama: String,
        harga: Double,
        stok: Int,
        kategori: String,
        gambarUrl: String,
        rating: Float,
        terjual: Int,
        deskripsi: String,
        spesifikasi: String
    ): Produk {
        // ID Konsisten (Slug)
        val consistentId = nama.trim().lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .replace(Regex("_{2,}"), "_")

        val finalImg = if (gambarUrl.isBlank()) IMG_DEFAULT else gambarUrl

        return Produk(
            id = consistentId,
            nama = nama,
            harga = harga,
            stok = stok,
            kategori = kategori,
            gambarUrl = finalImg,
            rating = rating,
            terjual = terjual,
            deskripsi = deskripsi,
            spesifikasi = spesifikasi
        )
    }

    private fun id(): String = java.util.UUID.randomUUID().toString() // Masih ada untuk fungsi lain jika perlu, tapi tidak dipakai createProduct
}