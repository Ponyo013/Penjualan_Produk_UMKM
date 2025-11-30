package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import com.example.penjualan_produk_umkm.database.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.model.Pesanan
import com.example.penjualan_produk_umkm.database.model.Produk
import com.example.penjualan_produk_umkm.database.model.StatusPesanan
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class DetailProdukFragment : Fragment(R.layout.fragment_detail_produk) {

    var produk: Produk? = null

    private lateinit var btnAddToCart: Button
    private lateinit var quantityControls: MaterialCardView
    private lateinit var btnIncrease: ImageButton
    private lateinit var btnDecrease: ImageButton
    private lateinit var tvQuantity: TextView

    private lateinit var cartViewModel: com.example.penjualan_produk_umkm.viewModel.CartViewModel
    private lateinit var produkViewModel: com.example.penjualan_produk_umkm.viewModel.ProdukViewModel

    // Placeholder for the current logged-in user ID. Replace with actual user ID retrieval.
    private val currentUserId = 1 

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val pesananDao = db.pesananDao()

        lifecycleScope.launch {
            // Try to find an existing pending order for the user
            var activePesanan = pesananDao.getPendingPesananForUser(currentUserId)
            var actualPesananId: Int

            if (activePesanan == null) {
                // If no pending order exists, create a new one
                val newPesanan = Pesanan(userId = currentUserId, totalHarga = 0.0, status = StatusPesanan.DIPROSES)
                actualPesananId = pesananDao.insert(newPesanan).toInt()
            } else {
                actualPesananId = activePesanan.id
            }

            cartViewModel = ViewModelProvider(
                this@DetailProdukFragment,
                ViewModelFactory(db = db, pesananId = actualPesananId)
            ).get(com.example.penjualan_produk_umkm.viewModel.CartViewModel::class.java)

            produkViewModel = ViewModelProvider(
                this@DetailProdukFragment,
                ViewModelFactory(produkDao = db.produkDao())
            ).get(com.example.penjualan_produk_umkm.viewModel.ProdukViewModel::class.java)

            val productId = arguments?.getInt("productId") ?: run {
                findNavController().popBackStack()
                return@launch
            }

            // Ambil produk dari database
            produkViewModel.getProdukById(productId).observe(viewLifecycleOwner) { p ->
                if (p == null) {
                    findNavController().popBackStack()
                } else {
                    produk = p
                    setupToolbar(view)
                    setupProductInfo(view, p, actualPesananId) // Pass actualPesananId here
                    setupRatingInfo(view, p)
                    setupViewPager(view)
                    setupGallery(view, p)
                    updateCartControls(p)
                }
            }

            // Observe cart items untuk update UI secara otomatis
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                    cartViewModel.cartItems.collectLatest { items ->
                        produk?.let { updateCartControls(it) }
                    }
                }
            }
        }
    }

    private fun setupProductInfo(view: View, produk: Produk, pesananId: Int) { // Add pesananId parameter
        val namaProduk = view.findViewById<TextView>(R.id.nama_produk)
        val hargaProduk = view.findViewById<TextView>(R.id.harga_produk)
        val stockStatus = view.findViewById<TextView>(R.id.stock_status)
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart)
        quantityControls = view.findViewById(R.id.quantity_controls)
        btnIncrease = view.findViewById(R.id.btn_increase_quantity)
        btnDecrease = view.findViewById(R.id.btn_decrease_quantity)
        tvQuantity = view.findViewById(R.id.tv_quantity)

        // Display info produk
        namaProduk.text = produk.nama
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        hargaProduk.text = formatRupiah.format(produk.harga).replace("Rp", "Rp ").trim()
        stockStatus.text = "Tersedia: ${produk.stok} Buah"
        stockStatus.setTextColor(resources.getColor(if (produk.stok > 0) R.color.Secondary_1 else R.color.red, null))
        btnAddToCart.isEnabled = produk.stok > 0

        // Tombol tambah ke keranjang
        btnAddToCart.setOnClickListener {
            val existingItem = cartViewModel.cartItems.value.find { it.produk.id == produk.id }
            if (existingItem != null) {
                cartViewModel.increaseQuantity(existingItem)
            } else {
                val newItem = ItemPesanan(0, 1, produk.id, 1) // Use the dynamic pesananId
                cartViewModel.insertItem(newItem) // Menggunakan insertItem
            }
            Toast.makeText(requireContext(), "Produk ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
        }

        // Tombol increase
        btnIncrease.setOnClickListener {
            val existingItem = cartViewModel.cartItems.value.find { it.produk.id == produk.id }
            existingItem?.let { cartViewModel.increaseQuantity(it) }
        }

        // Tombol decrease
        btnDecrease.setOnClickListener {
            val existingItem = cartViewModel.cartItems.value.find { it.produk.id == produk.id }
            existingItem?.let { cartViewModel.decreaseQuantity(it) }
        }
    }

    private fun updateCartControls(produk: Produk) {
        val existingItem = cartViewModel.cartItems.value.find { it.produk.id == produk.id }
        if (existingItem != null) {
            btnAddToCart.visibility = View.GONE
            quantityControls.visibility = View.VISIBLE
            tvQuantity.text = existingItem.itemPesanan.jumlah.toString()
        } else {
            btnAddToCart.visibility = View.VISIBLE
            quantityControls.visibility = View.GONE
        }
    }

    private fun setupRatingInfo(view: View, produk: Produk) {
        val ratingText = view.findViewById<TextView>(R.id.rating_text)
        val reviewCount = view.findViewById<TextView>(R.id.review_count)
        ratingText.text = String.format(Locale.US, "%.1f", produk.rating)
        reviewCount.text = "(${produk.terjual} terjual, 10 Ulasan)"
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_detailproduk)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_cart) {
                findNavController().navigate(R.id.action_detailProdukFragment_to_CartFragment)
                true
            } else false
        }
    }

    private fun setupViewPager(view: View) {
        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        val adapter = DetailPagerAdapter(this)
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

    private fun setupGallery(view: View, produk: Produk) {
        val galleryViewPager = view.findViewById<ViewPager2>(R.id.gallery_view_pager)
        val imageUrls = produk.gambarResourceIds.map { it.toString() }

        val galleryAdapter = GalleryAdapter(imageUrls) { position ->
            // klik untuk full screen, jika mau bisa ditambahkan dialog
        }
        galleryViewPager.adapter = galleryAdapter
    }
}
