package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class DetailProdukFragment : Fragment(R.layout.fragment_detail_produk) {

    private var produk: Produk? = null

    private lateinit var btnAddToCart: Button
    private lateinit var quantityControls: android.widget.LinearLayout
    private lateinit var btnIncrease: ImageButton
    private lateinit var btnDecrease: ImageButton
    private lateinit var tvQuantity: TextView
    private lateinit var tvEstimatedTotal: TextView
    private var currentQuantity = 1
    private var badgeTextView: TextView? = null
    // Inisialisasi ViewModel dengan Factory Kosong (Firebase)
    private val cartViewModel: CartViewModel by viewModels { ViewModelFactory() }
    private val produkViewModel: ProdukViewModel by viewModels { ViewModelFactory() }

    private var produkListener: ListenerRegistration? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIX: Ambil ID Produk sebagai String (Firebase)
        val productId = arguments?.getString("productId") ?: run {
            findNavController().popBackStack()
            return
        }

        // Ambil produk dari Firestore
        // Menggunakan callback karena di Firebase ViewModel getProdukById itu async callback, bukan LiveData
        produkViewModel.getProdukById(productId) { p ->
            if (p == null) {
                Toast.makeText(context, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                produk = p

                // untuk real time
                listenProdukRealtime(productId, view)

                setupToolbar(view)
                setupProductInfo(view, p)
                setupRatingInfo(view, p)
                setupViewPager(view, p.id) // Kirim ID Produk untuk Pager Adapter
                setupGallery(view, p)

                // Update status tombol cart awal (nanti diupdate lagi oleh flow)
                updateCartControls(p, emptyList())
            }
        }

        // Observe cart items untuk update UI secara otomatis (Realtime)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                cartViewModel.totalQuantity.collectLatest { totalQty ->
                    // Update Badge di Toolbar
                    if (totalQty > 0) {
                        badgeTextView?.visibility = View.VISIBLE
                        badgeTextView?.text = if (totalQty > 99) "99+" else totalQty.toString()
                    } else {
                        badgeTextView?.visibility = View.GONE
                    }

                    // Update logika tombol "Tambah" di bawah juga (kode lama)
                    produk?.let { updateCartControls(it, cartViewModel.cartItems.value) }
                }
            }
        }
    }

    private fun setupProductInfo(view: View, produk: Produk) {
        val namaProduk = view.findViewById<TextView>(R.id.nama_produk)
        val hargaProduk = view.findViewById<TextView>(R.id.harga_produk)
        val stockStatus = view.findViewById<TextView>(R.id.stock_status)
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart)
        quantityControls = view.findViewById(R.id.quantity_controls)
        btnIncrease = view.findViewById(R.id.btn_increase_quantity)
        btnDecrease = view.findViewById(R.id.btn_decrease_quantity)
        tvQuantity = view.findViewById(R.id.tv_quantity)
        tvEstimatedTotal = view.findViewById(R.id.tv_estimated_total)
        tvQuantity = view.findViewById(R.id.tv_quantity)

        currentQuantity = 1
        updateTotalPriceDisplay(produk.harga)



        // Display info produk
        namaProduk.text = produk.nama
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatRupiah.maximumFractionDigits = 0
        hargaProduk.text = formatRupiah.format(produk.harga).replace("Rp", "Rp ").trim()

        stockStatus.text = "Tersedia: ${produk.stok} Buah"
        stockStatus.setTextColor(resources.getColor(if (produk.stok > 0) R.color.Secondary_1 else R.color.red, null))
        btnAddToCart.isEnabled = produk.stok > 0

        // Tombol tambah ke keranjang
        btnAddToCart.setOnClickListener {
            if (produk.stok <= 0) {
                Toast.makeText(requireContext(), "Stok habis", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek apakah item sudah ada di keranjang (list item dari viewModel)
            val currentItems = cartViewModel.cartItems.value
            val existingItem = currentItems.find { it.produkId == produk.id }

            if (existingItem != null) {
                val updatedItem = existingItem.copy(jumlah = currentQuantity)
                cartViewModel.updateItem(updatedItem)

                Toast.makeText(requireContext(), "Keranjang diperbarui menjadi $currentQuantity items", Toast.LENGTH_SHORT).show()
            } else {
                // Insert Item Baru
                // ID pesanan akan dihandle otomatis oleh ViewModel (mencari pesanan aktif)
                val pesananAktifId = cartViewModel.pesanan.value?.id ?: ""

                // Jika pesanan belum ada, ViewModel biasanya handle, tapi kita buat object item dulu
                val newItem = ItemPesanan(
                    id = "", // ID di-generate di ViewModel/Firestore
                    pesananId = pesananAktifId, // Nanti diset ulang di ViewModel jika kosong
                    produkId = produk.id,
                    produkNama = produk.nama,
                    produkHarga = produk.harga,
                    gambarUrl = produk.gambarUrl,
                    jumlah = currentQuantity,
                    isSelected = true
                )
                cartViewModel.insertItem(newItem)
                Toast.makeText(requireContext(), "Produk ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
            }

        }

        // Tombol increase
        btnIncrease.setOnClickListener {
            if (currentQuantity < produk.stok) {
                currentQuantity++
                tvQuantity.text = currentQuantity.toString()
                updateTotalPriceDisplay(produk.harga)
            } else {
                Toast.makeText(context, "Stok maksimal tercapai", Toast.LENGTH_SHORT).show()
            }
        }

        btnDecrease.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                tvQuantity.text = currentQuantity.toString()
                updateTotalPriceDisplay(produk.harga)
            }
        }
    }

    // FIX: Parameter kedua menerima List<ItemPesanan> Firestore
    private fun updateCartControls(produk: Produk, items: List<ItemPesanan>) {
        val existingItem = items.find { it.produkId == produk.id }

        if (existingItem != null) {
            // Jika sudah ada di keranjang, set counter ke jumlah yang ada
            currentQuantity = existingItem.jumlah
            tvQuantity.text = currentQuantity.toString()
            btnAddToCart.text = "Update Keranjang" // Ubah teks tombol biar jelas
        } else {
            // Jika belum ada, reset ke 1
            currentQuantity = 1
            tvQuantity.text = "1"
            btnAddToCart.text = "Tambah ke Keranjang"
        }
        // Update total harga berdasarkan quantity yang baru diset
        updateTotalPriceDisplay(produk.harga)
    }

    private fun setupRatingInfo(view: View, produk: Produk) {
        val ratingText = view.findViewById<TextView>(R.id.rating_text)
        val reviewCount = view.findViewById<TextView>(R.id.review_count)
        // Asumsikan Anda punya view panah di layout, misalnya ID-nya 'btn_rating_arrow' atau container-nya
        // Jika layout Anda menggunakan 'include' atau layout manual, pastikan Anda mencari view yang bisa diklik.

        // Mencari view container rating atau panah chevron
        // (Sesuaikan ID ini dengan XML 'fragment_detail_produk.xml' atau 'item_rating.xml' Anda)
        val ratingContainer = view.findViewById<View>(R.id.rating_container)
        // ATAU cari panahnya langsung:
        // val arrowIcon = view.findViewById<ImageView>(R.id.iv_chevron_right)

        ratingText.text = String.format(Locale.US, "%.1f", produk.rating)
        reviewCount.text = "(${produk.terjual} terjual)"

        // Tambahkan aksi klik
        ratingContainer?.setOnClickListener {
            // Pindah ke Tab ke-2 (Ulasan)
            // Index: 0=Deskripsi, 1=Spesifikasi, 2=Ulasan
            val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
            viewPager.currentItem = 2
        }
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_detailproduk)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // --- SETUP LOGIC BADGE ---
        val menuItem = toolbar.menu.findItem(R.id.action_cart)
        val actionView = menuItem.actionView

        // 1. Ambil referensi ke TextView badge
        badgeTextView = actionView?.findViewById(R.id.tv_cart_badge_toolbar)

        // 2. Pasang Click Listener manual karena pakai actionLayout
        actionView?.setOnClickListener {
            findNavController().navigate(R.id.action_detailProdukFragment_to_CartFragment)
        }
    }

    // FIX: Menerima String productId
    private fun setupViewPager(view: View, productId: String) {
        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)

        // Pastikan DetailPagerAdapter Anda sudah diupdate constructor-nya menerima String
        val adapter = DetailPagerAdapter(this, productId)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Deskripsi"
                1 -> "Spesifikasi"
                2 -> "Ulasan"
                else -> ""
            }
        }.attach()
    }

    // Fungsi Helper untuk update teks harga
    private fun updateTotalPriceDisplay(hargaSatuan: Double) {
        val total = hargaSatuan * currentQuantity
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatRupiah.maximumFractionDigits = 0
        tvEstimatedTotal.text = formatRupiah.format(total).replace("Rp", "Rp ").trim()
    }

    private fun setupGallery(view: View, produk: Produk) {
        val galleryViewPager = view.findViewById<ViewPager2>(R.id.gallery_view_pager)

        // FIX: Menggunakan gambarUrl (String)
        // Masukkan ke list karena GalleryAdapter mungkin menerima List<String>
        val imageUrls = if (produk.gambarUrl.isNotEmpty()) listOf(produk.gambarUrl) else emptyList()

        val galleryAdapter = GalleryAdapter(imageUrls) { position ->
            // klik untuk full screen
        }
        galleryViewPager.adapter = galleryAdapter
    }

    private fun listenProdukRealtime(produkId: String, view: View) {
        val db = FirebaseFirestore.getInstance()
        val produkRef = db.collection("produk").document(produkId)

        produkListener = produkRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {

                val produkUpdated = snapshot.toObject(Produk::class.java)
                if (produkUpdated != null) {

                    // Update data lokal
                    produk = produkUpdated

                    // 🔥 UI update real-time!
                    setupRatingInfo(view, produkUpdated)
                    setupProductInfo(view, produkUpdated)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        produkListener?.remove()
    }

}