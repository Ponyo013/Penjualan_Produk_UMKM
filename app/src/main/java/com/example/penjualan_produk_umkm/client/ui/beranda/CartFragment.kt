package com.example.penjualan_produk_umkm.client.ui.beranda

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.auth.UserPreferences
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private lateinit var rvCart: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var totalPrice: TextView
    private lateinit var checkoutButton: MaterialButton

    // FIX 1: Kita inisialisasi langsung di sini dengan ID 1.
    // Tidak perlu logic ribet cari ID di onViewCreated.
    private val cartViewModel: CartViewModel by viewModels {
        ViewModelFactory(
            db = AppDatabase.getDatabase(requireContext()),
            pesananId = 1 // <--- KUNCI: Paksa buka Keranjang ID 1 agar data pasti muncul
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        rvCart = view.findViewById(R.id.rv_cart_products)
        totalPrice = view.findViewById(R.id.tv_total_price)
        checkoutButton = view.findViewById(R.id.btn_checkout)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.cartItems.collectLatest { items ->
                cartAdapter.updateCartItems(items)
                updateCheckoutButtonState(items)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.pesanan.collectLatest { pesanan ->
                val localeID = Locale("in", "ID")
                val numberFormat = NumberFormat.getCurrencyInstance(localeID)
                numberFormat.maximumFractionDigits = 0
                totalPrice.text = numberFormat.format(pesanan?.totalHarga ?: 0.0)
            }
        }
    }

    private fun setupClickListeners() {
        checkoutButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val items = cartViewModel.cartItems.value
                val selectedItems = items.filter { it.itemPesanan.isSelected }

                if (selectedItems.isNotEmpty()) {
                    val selectedIds = selectedItems.map { it.itemPesanan.id }.toIntArray()
                    val bundle = Bundle().apply {
                        putIntArray("selectedItemIds", selectedIds)
                    }
                    findNavController().navigate(
                        R.id.action_CartFragment_to_checkoutFragment,
                        bundle
                    )
                }
            }
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            mutableListOf(),
            onIncrease = { item -> cartViewModel.increaseQuantity(item) },
            onDecrease = { item ->
                if (item.itemPesanan.jumlah > 1) {
                    cartViewModel.decreaseQuantity(item)
                } else {
                    showDeleteConfirmationDialog(item)
                }
            },
            // FIX 2: Perbaikan logika Checkbox agar tombol checkout bisa nyala
            onItemSelectChanged = { item ->
                // Langsung update item yang dikirim adapter (karena adapter sudah mengubah status true/false-nya)
                lifecycleScope.launch {
                    cartViewModel.updateItem(item.itemPesanan)
                }
            },
            onItemClick = { item ->
                val bundle = Bundle()
                bundle.putInt("productId", item.produk.id)
                findNavController().navigate(
                    R.id.action_CartFragment_to_detailProdukFragment,
                    bundle
                )
            }
        )

        rvCart.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun showDeleteConfirmationDialog(item: ItemPesananWithProduk) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_warning, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnPositive = dialogView.findViewById<MaterialButton>(R.id.btn_positive)
        val btnNegative = dialogView.findViewById<MaterialButton>(R.id.btn_negative)

        btnPositive.setOnClickListener {
            lifecycleScope.launch {
                cartViewModel.removeItem(item)
            }
            dialog.dismiss()
        }

        btnNegative.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun updateCheckoutButtonState(items: List<ItemPesananWithProduk>) {
        // Tombol hanya aktif jika ada minimal 1 barang yang dicentang (isSelected == true)
        val anySelected = items.any { it.itemPesanan.isSelected }
        checkoutButton.isEnabled = anySelected

        val color = if (anySelected) R.color.Secondary_1 else R.color.grey
        checkoutButton.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }
}