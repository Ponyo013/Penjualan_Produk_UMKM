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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
// Import Model Firestore
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import androidx.fragment.app.activityViewModels

class CartFragment : Fragment() {

    private lateinit var rvCart: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var totalPrice: TextView
    private lateinit var checkoutButton: MaterialButton

    // FIX 1: Gunakan Factory Kosong (Firebase)
    // ViewModel akan otomatis mencari keranjang aktif user dari Firestore
    private val cartViewModel: CartViewModel by activityViewModels { ViewModelFactory() }

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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observasi Item Keranjang
                launch {
                    cartViewModel.cartItems.collectLatest { items ->
                        cartAdapter.updateCartItems(items)
                        updateCheckoutButtonState(items)
                    }
                }

                // Observasi Total Harga Pesanan
                launch {
                    cartViewModel.pesanan.collectLatest { pesanan ->
                        val localeID = Locale("in", "ID")
                        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
                        numberFormat.maximumFractionDigits = 0
                        totalPrice.text = numberFormat.format(pesanan?.totalHarga ?: 0.0)
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        checkoutButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val items = cartViewModel.cartItems.value
                val selectedItems = items.filter { it.isSelected }

                if (selectedItems.isNotEmpty()) {
                    // FIX 2: ID di Firebase adalah String, gunakan putStringArray (atau ArrayList<String>)
                    // Tapi Bundle tidak support StringArray secara langsung untuk navigasi safeargs kadang,
                    // namun untuk Bundle manual bisa.
                    // Kita kirim array string ID item.
                    val selectedIds = selectedItems.map { it.id }.toTypedArray()

                    val bundle = Bundle().apply {
                        putStringArray("selectedItemIds", selectedIds)
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
        // FIX 3: Adapter sekarang menerima List<ItemPesanan> (Model Firestore)
        cartAdapter = CartAdapter(
            mutableListOf(),
            onIncrease = { item -> cartViewModel.increaseQuantity(item) },
            onDecrease = { item ->
                if (item.jumlah > 1) {
                    cartViewModel.decreaseQuantity(item)
                } else {
                    showDeleteConfirmationDialog(item)
                }
            },
            onItemSelectChanged = { item ->
                // Update status checkbox ke Firestore
                // Item yang dikirim dari adapter sudah memiliki status isSelected terbaru
                cartViewModel.updateItem(item)
            },
            onItemClick = { item ->
                val bundle = Bundle()
                // FIX 4: ID Produk adalah String
                bundle.putString("productId", item.produkId)
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

    // FIX 5: Parameter ItemPesanan (Firestore)
    private fun showDeleteConfirmationDialog(item: ItemPesanan) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_warning, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnPositive = dialogView.findViewById<MaterialButton>(R.id.btn_positive)
        val btnNegative = dialogView.findViewById<MaterialButton>(R.id.btn_negative)

        btnPositive.setOnClickListener {
            cartViewModel.removeItem(item)
            dialog.dismiss()
        }

        btnNegative.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // FIX 6: Parameter List<ItemPesanan>
    private fun updateCheckoutButtonState(items: List<ItemPesanan>) {
        val anySelected = items.any { it.isSelected }
        checkoutButton.isEnabled = anySelected

        val color = if (anySelected) R.color.Secondary_1 else R.color.grey
        checkoutButton.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }
}