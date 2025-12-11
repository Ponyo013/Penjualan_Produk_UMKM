package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Pastikan ini ada
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
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

    // Gunakan activityViewModels agar state keranjang tetap hidup antar fragment
    private val cartViewModel: CartViewModel by activityViewModels { ViewModelFactory() }
    private val produkViewModel: ProdukViewModel by viewModels { ViewModelFactory() }

    private var produkListener: ListenerRegistration? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getString("productId") ?: run {
            findNavController().popBackStack()
            return
        }

        // --- PERBAIKAN 1: Panggil setupToolbar SEGERA ---
        // Jangan taruh di dalam callback produkViewModel.
        // Toolbar harus siap sebelum observer berjalan.
        setupToolbar(view)

        // Load data produk
        produkViewModel.getProdukById(productId) { p ->
            if (p == null) {
                Toast.makeText(context, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                produk = p
                listenProdukRealtime(productId, view)

                setupProductInfo(view, p)
                setupRatingInfo(view, p)
                setupViewPager(view, p.id)
                setupGallery(view, p)

                // Update kontrol tombol bawah
                updateCartControls(p, cartViewModel.cartItems.value)
            }
        }

        // --- PERBAIKAN 2: Observasi Badge Keranjang ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartViewModel.totalQuantity.collectLatest { totalQty ->

                    // Ambil Toolbar & Menu Item
                    val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
                    val menuItem = toolbar.menu.findItem(R.id.action_cart)

                    if (menuItem != null) {
                        // Ambil Action View (Layout kustom badge)
                        val actionView = menuItem.actionView

                        // Jika actionView null (karena belum inflate atau error), tidak apa-apa skip
                        if (actionView != null) {
                            val badgeTv = actionView.findViewById<TextView>(R.id.tv_cart_badge_toolbar)

                            // Update UI Badge
                            if (badgeTv != null) {
                                if (totalQty > 0) {
                                    badgeTv.visibility = View.VISIBLE
                                    badgeTv.text = if (totalQty > 99) "99+" else totalQty.toString()
                                } else {
                                    badgeTv.visibility = View.GONE
                                }
                            }

                            // --- PENTING: Pasang Listener Klik Manual ---
                            // Karena actionLayout menimpa behavior default menu,
                            // onOptionsItemSelected tidak akan terpanggil otomatis.
                            actionView.setOnClickListener {
                                findNavController().navigate(R.id.action_detailProdukFragment_to_CartFragment)
                            }
                        }
                    }

                    // Update tombol bawah (Tambah ke Keranjang) jika produk sudah dimuat
                    produk?.let { updateCartControls(it, cartViewModel.cartItems.value) }
                }
            }
        }
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        // Bersihkan menu lama untuk menghindari duplikasi saat Fragment di-recreate
        toolbar.menu.clear()

        // Inflate menu baru yang sudah berisi actionLayout badge
        toolbar.inflateMenu(R.menu.menu_detailproduk)

        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    // ... (Sisa fungsi setupProductInfo, updateCartControls, dll. TETAP SAMA seperti kode Anda) ...
    // ... Copy-paste saja bagian bawah ini dari kode lama Anda ...

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

        currentQuantity = 1
        updateTotalPriceDisplay(produk.harga)

        namaProduk.text = produk.nama
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatRupiah.maximumFractionDigits = 0
        hargaProduk.text = formatRupiah.format(produk.harga).replace("Rp", "Rp ").trim()

        stockStatus.text = "Tersedia: ${produk.stok} Buah"
        stockStatus.setTextColor(resources.getColor(if (produk.stok > 0) R.color.Secondary_1 else R.color.red, null))
        btnAddToCart.isEnabled = produk.stok > 0

        btnAddToCart.setOnClickListener {
            if (produk.stok <= 0) {
                Toast.makeText(requireContext(), "Stok habis", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentItems = cartViewModel.cartItems.value
            val existingItem = currentItems.find { it.produkId == produk.id }

            if (existingItem != null) {
                val updatedItem = existingItem.copy(jumlah = currentQuantity)
                cartViewModel.updateItem(updatedItem)
                Toast.makeText(requireContext(), "Keranjang diperbarui menjadi $currentQuantity items", Toast.LENGTH_SHORT).show()
            } else {
                val pesananAktifId = cartViewModel.pesanan.value?.id ?: ""
                val newItem = ItemPesanan(
                    id = "",
                    pesananId = pesananAktifId,
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

    private fun updateCartControls(produk: Produk, items: List<ItemPesanan>) {
        val existingItem = items.find { it.produkId == produk.id }
        if (existingItem != null) {
            currentQuantity = existingItem.jumlah
            tvQuantity.text = currentQuantity.toString()
            btnAddToCart.text = "Update Keranjang"
        } else {
            currentQuantity = 1
            tvQuantity.text = "1"
            btnAddToCart.text = "Tambah ke Keranjang"
        }
        updateTotalPriceDisplay(produk.harga)
    }

    private fun setupRatingInfo(view: View, produk: Produk) {
        val ratingText = view.findViewById<TextView>(R.id.rating_text)
        val reviewCount = view.findViewById<TextView>(R.id.review_count)
        val ratingContainer = view.findViewById<View>(R.id.rating_container)

        ratingText.text = String.format(Locale.US, "%.1f", produk.rating)
        reviewCount.text = "(${produk.terjual} terjual)"

        ratingContainer?.setOnClickListener {
            val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
            viewPager.currentItem = 2
        }
    }

    private fun setupViewPager(view: View, productId: String) {
        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
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

    private fun updateTotalPriceDisplay(hargaSatuan: Double) {
        val total = hargaSatuan * currentQuantity
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatRupiah.maximumFractionDigits = 0
        tvEstimatedTotal.text = formatRupiah.format(total).replace("Rp", "Rp ").trim()
    }

    private fun setupGallery(view: View, produk: Produk) {
        val galleryViewPager = view.findViewById<ViewPager2>(R.id.gallery_view_pager)
        val imageUrls = if (produk.gambarUrl.isNotEmpty()) listOf(produk.gambarUrl) else emptyList()
        val galleryAdapter = GalleryAdapter(imageUrls) { position -> }
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
                    produk = produkUpdated
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