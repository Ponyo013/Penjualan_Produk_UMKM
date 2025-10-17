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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.dummyItems
import com.example.penjualan_produk_umkm.model.ItemPesanan
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private lateinit var rvCart: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var totalPrice: TextView
    private lateinit var checkoutButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        rvCart = view.findViewById(R.id.rv_cart_products)
        totalPrice = view.findViewById(R.id.tv_total_price)
        checkoutButton = view.findViewById(R.id.btn_checkout)

        setupRecyclerView()
        updateTotalPrice()
        updateCheckoutButtonState()

        checkoutButton.setOnClickListener {
            val selectedItems = dummyItems.filter { it.isSelected }.toTypedArray()
            val bundle = Bundle()
            bundle.putParcelableArray("cartItems", selectedItems)
            findNavController().navigate(R.id.action_CartFragment_to_checkoutFragment, bundle)
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(dummyItems, {
            // On Increase
            val index = dummyItems.indexOf(it)
            if (index != -1) {
                dummyItems[index].jumlah++
                cartAdapter.notifyItemChanged(index)
                updateTotalPrice()
            }
        }, {
            // On Decrease
            val index = dummyItems.indexOf(it)
            if (index != -1) {
                if (dummyItems[index].jumlah > 1) {
                    dummyItems[index].jumlah--
                    cartAdapter.notifyItemChanged(index)
                    updateTotalPrice()
                } else {
                    showDeleteConfirmationDialog(index)
                }
            }
        }, {
            // On Item Select Changed
            val index = dummyItems.indexOf(it)
            if (index != -1) {
                dummyItems[index].isSelected = !dummyItems[index].isSelected
                cartAdapter.notifyItemChanged(index)
                updateTotalPrice()
                updateCheckoutButtonState()
            }
        })
        rvCart.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showDeleteConfirmationDialog(index: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_warning, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnPositive = dialogView.findViewById<MaterialButton>(R.id.btn_positive)
        val btnNegative = dialogView.findViewById<MaterialButton>(R.id.btn_negative)

        btnPositive.setOnClickListener {
            dummyItems.removeAt(index)
            cartAdapter.notifyItemRemoved(index)
            updateTotalPrice()
            updateCheckoutButtonState()
            dialog.dismiss()
        }

        btnNegative.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun updateTotalPrice() {
        val total = dummyItems.filter { it.isSelected }.sumOf { it.subtotal }
        val localeID = Locale.Builder().setLanguage("id").setRegion("ID").build()
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        totalPrice.text = numberFormat.format(total)
    }

    private fun updateCheckoutButtonState() {
        val anySelected = dummyItems.any { it.isSelected }
        checkoutButton.isEnabled = anySelected
        if (anySelected) {
            checkoutButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.Secondary_1))
        } else {
            checkoutButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey))
        }
    }
}
