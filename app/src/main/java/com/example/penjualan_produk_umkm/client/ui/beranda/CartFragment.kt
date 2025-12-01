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
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
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

    // CartViewModel sekarang ambil userId sendiri dari FirebaseAuth
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCart = view.findViewById(R.id.rv_cart_products)
        totalPrice = view.findViewById(R.id.tv_total_price)
        checkoutButton = view.findViewById(R.id.btn_checkout)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            mutableListOf(),
            onIncrease = { item -> cartViewModel.increaseQuantity(item) },
            onDecrease = { item -> cartViewModel.decreaseQuantity(item) },
            onItemSelectChanged = { item -> cartViewModel.updateItem(item) },
            onItemClick = { item ->
                val bundle = Bundle()
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

    private fun setupObservers() {
        lifecycleScope.launch {
            cartViewModel.cartItems.collectLatest { items ->
                cartAdapter.updateCartItems(items)
                updateCheckoutButtonState(items)
            }
        }

        lifecycleScope.launch {
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
            lifecycleScope.launch {
                val selectedItems = cartViewModel.cartItems.value.filter { it.isSelected }
                if (selectedItems.isNotEmpty()) {
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

    private fun updateCheckoutButtonState(items: List<ItemPesanan>) {
        val anySelected = items.any { it.isSelected }
        checkoutButton.isEnabled = anySelected

        val color = if (anySelected) R.color.Secondary_1 else R.color.grey
        checkoutButton.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }

    private fun showDeleteConfirmationDialog(item: ItemPesanan) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_warning, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnPositive = dialogView.findViewById<MaterialButton>(R.id.btn_positive)
        val btnNegative = dialogView.findViewById<MaterialButton>(R.id.btn_negative)

        btnPositive.setOnClickListener {
            lifecycleScope.launch { cartViewModel.removeItem(item) }
            dialog.dismiss()
        }

        btnNegative.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
