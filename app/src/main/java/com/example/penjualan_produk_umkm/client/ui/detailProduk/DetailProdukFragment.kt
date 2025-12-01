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
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class DetailProdukFragment : Fragment(R.layout.fragment_detail_produk) {

    private var produk: Produk? = null

    private lateinit var btnAddToCart: Button
    private lateinit var quantityControls: MaterialCardView
    private lateinit var btnIncrease: ImageButton
    private lateinit var btnDecrease: ImageButton
    private lateinit var tvQuantity: TextView

    private val cartViewModel: CartViewModel by viewModels()
    private val produkViewModel: ProdukViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getString("productId") ?: run {
            findNavController().popBackStack()
            return
        }

        setupToolbar(view)
        setupViewPager(view)
        setupViews(view)

        // Ambil produk dari Firestore
        produkViewModel.getProdukById(productId) { p ->
            if (p == null) {
                findNavController().popBackStack()
            } else {
                produk = p
                displayProdukInfo(p)
            }
        }

        // Observe cart items untuk update UI secara real-time
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                cartViewModel.cartItems.collectLatest { items ->
                    produk?.let { updateCartControls(it, items) }
                }
            }
        }
    }

    private fun setupViews(view: View) {
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart)
        quantityControls = view.findViewById(R.id.quantity_controls)
        btnIncrease = view.findViewById(R.id.btn_increase_quantity)
        btnDecrease = view.findViewById(R.id.btn_decrease_quantity)
        tvQuantity = view.findViewById(R.id.tv_quantity)
    }

    private fun displayProdukInfo(produk: Produk) {
        val namaProduk = requireView().findViewById<TextView>(R.id.nama_produk)
        val hargaProduk = requireView().findViewById<TextView>(R.id.harga_produk)
        val stockStatus = requireView().findViewById<TextView>(R.id.stock_status)

        namaProduk.text = produk.nama
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        hargaProduk.text = formatRupiah.format(produk.harga).replace("Rp", "Rp ").trim()
        stockStatus.text = "Tersedia: ${produk.stok} Buah"
        stockStatus.setTextColor(
            resources.getColor(
                if (produk.stok > 0) R.color.Secondary_1 else R.color.red,
                null
            )
        )
        btnAddToCart.isEnabled = produk.stok > 0

        btnAddToCart.setOnClickListener {
            val existingItem = cartViewModel.cartItems.value.find { it.produkId == produk.id }
            if (existingItem != null) {
                cartViewModel.increaseQuantity(existingItem)
            } else {
                val pesananId = cartViewModel.pesanan.value?.id ?: return@setOnClickListener
                val newItem = ItemPesanan(
                    id = "", // akan di-generate di insertItem
                    pesananId = pesananId,
                    jumlah = 1,
                    produkId = produk.id,
                    produkHarga = produk.harga,
                    isSelected = true
                )
                cartViewModel.insertItem(newItem)
            }
            Toast.makeText(requireContext(), "Produk ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
        }

        btnIncrease.setOnClickListener {
            val existingItem = cartViewModel.cartItems.value.find { it.produkId == produk.id }
            existingItem?.let { cartViewModel.increaseQuantity(it) }
        }

        btnDecrease.setOnClickListener {
            val existingItem = cartViewModel.cartItems.value.find { it.produkId == produk.id }
            existingItem?.let { cartViewModel.decreaseQuantity(it) }
        }
    }

    private fun updateCartControls(produk: Produk, items: List<ItemPesanan>) {
        val existingItem = items.find { it.produkId == produk.id }
        if (existingItem != null) {
            btnAddToCart.visibility = View.GONE
            quantityControls.visibility = View.VISIBLE
            tvQuantity.text = existingItem.jumlah.toString()
        } else {
            btnAddToCart.visibility = View.VISIBLE
            quantityControls.visibility = View.GONE
        }
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
        produk?.let { p ->
            val adapter = DetailPagerAdapter(this, p.id) // kirim fragment + produkId
            val viewPager: ViewPager2 = requireView().findViewById(R.id.view_pager)
            viewPager.adapter = adapter
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Deskripsi"
                1 -> "Spesifikasi"
                2 -> "Ulasan"
                else -> ""
            }
        }.attach()
    }
}
