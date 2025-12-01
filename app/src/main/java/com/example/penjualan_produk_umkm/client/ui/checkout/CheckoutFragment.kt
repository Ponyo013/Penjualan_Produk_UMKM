package com.example.penjualan_produk_umkm.client.ui.checkout

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.client.ui.beranda.CartAdapter
import com.example.penjualan_produk_umkm.database.firestore.model.MetodePembayaran
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Ekspedisi
import com.example.penjualan_produk_umkm.viewModel.CheckoutViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private lateinit var cartAdapter: CartAdapter
    private var selectedShippingCost: Double = 0.0
    private var selectedPaymentMethod: MetodePembayaran = MetodePembayaran.CASH

    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserAddress: TextView

    private val viewModel: CheckoutViewModel by viewModels()
    private var cartItems: List<ItemPesanan> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserPhone = view.findViewById(R.id.tv_user_phone)
        tvUserAddress = view.findViewById(R.id.tv_user_address)

        view.findViewById<ImageButton>(R.id.btn_edit_address).setOnClickListener {
            showEditAddressDialog()
        }

        setupRecyclerView(view)
        setupObservers(view)
        setupShippingOptions(view)
        setupPaymentMethods(view)

        view.findViewById<MaterialButton>(R.id.btn_bayar).setOnClickListener {
            checkout()
        }
    }

    private fun setupRecyclerView(view: View) {
        val rvProdukCheckout = view.findViewById<RecyclerView>(R.id.rv_produk_checkout)

        cartAdapter = CartAdapter(
            cartItems = mutableListOf(),
            onIncrease = {},
            onDecrease = {},
            onItemSelectChanged = {},
            onItemClick = {}
        )

        rvProdukCheckout.layoutManager = LinearLayoutManager(requireContext())
        rvProdukCheckout.adapter = cartAdapter
    }


    private fun setupObservers(view: View) {
        // Observe user data
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                tvUserName.text = it.nama
                tvUserPhone.text = it.noTelepon
                tvUserAddress.text = it.alamat
            }
        }

        // Observe cart items
        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartItems = items.filter { it.isSelected }
            cartAdapter.updateCartItems(cartItems)
            updatePaymentDetails(view)
        }
    }

    private fun setupShippingOptions(view: View) {
        viewModel.ekspedisiAktif.observe(viewLifecycleOwner) { ekspedisiList ->
            val ekspedisiItems = ekspedisiList.map { "${it.nama} (${it.estimasiHari} hari) - Rp ${String.format("%,.0f", it.biaya)}" }
            val actEkspedisi = view.findViewById<AutoCompleteTextView>(R.id.act_ekspedisi)
            val ekspedisiAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ekspedisiItems)
            actEkspedisi.setAdapter(ekspedisiAdapter)

            if (ekspedisiList.isNotEmpty()) {
                selectedShippingCost = ekspedisiList[0].biaya
                actEkspedisi.setText(ekspedisiItems[0], false)
                updatePaymentDetails(view)
            }

            actEkspedisi.setOnItemClickListener { _, _, position, _ ->
                selectedShippingCost = ekspedisiList[position].biaya
                updatePaymentDetails(view)
            }
        }
    }

    private fun setupPaymentMethods(view: View) {
        val paymentMethodItems = MetodePembayaran.values().map { it.name }
        val actMetodePembayaran = view.findViewById<AutoCompleteTextView>(R.id.act_metode_pembayaran)
        val paymentMethodAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, paymentMethodItems)
        actMetodePembayaran.setAdapter(paymentMethodAdapter)
        actMetodePembayaran.setText(paymentMethodItems[0], false)
        actMetodePembayaran.setOnItemClickListener { _, _, position, _ ->
            selectedPaymentMethod = MetodePembayaran.values()[position]
        }
    }

    private fun updatePaymentDetails(view: View) {
        val subtotal = cartItems.sumOf { it.jumlah * it.produkHarga }
        val total = subtotal + selectedShippingCost

        view.findViewById<TextView>(R.id.tv_subtotal_produk).text = "Rp ${String.format("%,.0f", subtotal)}"
        view.findViewById<TextView>(R.id.tv_biaya_pengiriman).text = "Rp ${String.format("%,.0f", selectedShippingCost)}"
        view.findViewById<TextView>(R.id.tv_total_pembayaran).text = "Rp ${String.format("%,.0f", total)}"
    }

    private fun checkout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Keranjang kosong atau tidak ada barang dipilih!", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedEkspedisi = viewModel.ekspedisiAktif.value?.find { it.biaya == selectedShippingCost }
        if (selectedEkspedisi == null) {
            Toast.makeText(requireContext(), "Mohon pilih ekspedisi", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createPesanan(
            items = cartItems,
            ekspedisi = selectedEkspedisi,
            metodePembayaran = selectedPaymentMethod,
            onSuccess = {
                Toast.makeText(requireContext(), "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.BerandaFragment, false)
            },
            onError = { errorMsg ->
                Toast.makeText(requireContext(), "Gagal membuat pesanan: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showEditAddressDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_address, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvDialogName = dialogView.findViewById<TextInputEditText>(R.id.tv_dialog_name)
        val tvDialogPhone = dialogView.findViewById<TextInputEditText>(R.id.tv_dialog_phone)
        val etDialogAddress = dialogView.findViewById<TextInputEditText>(R.id.et_dialog_address)

        tvDialogName.setText(tvUserName.text)
        tvDialogPhone.setText(tvUserPhone.text)
        etDialogAddress.setText(tvUserAddress.text)

        dialogView.findViewById<MaterialButton>(R.id.btn_dialog_save).setOnClickListener {
            val newName = tvDialogName.text.toString()
            val newPhone = tvDialogPhone.text.toString()
            val newAddress = etDialogAddress.text.toString()

            if (newName.isBlank()) { tvDialogName.error = "Nama tidak boleh kosong"; return@setOnClickListener }
            if (newPhone.isBlank()) { tvDialogPhone.error = "No. Telepon tidak boleh kosong"; return@setOnClickListener }
            if (newAddress.isBlank()) { etDialogAddress.error = "Alamat tidak boleh kosong"; return@setOnClickListener }

            viewModel.updateUserAddress(newName, newPhone, newAddress)
            Toast.makeText(requireContext(), "Alamat berhasil diperbarui", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btn_dialog_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
