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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.client.ui.beranda.CartAdapter
import com.example.penjualan_produk_umkm.dummyItems
import com.example.penjualan_produk_umkm.dummyPesanan
import com.example.penjualan_produk_umkm.dummyUsers
import com.example.penjualan_produk_umkm.ekspedisiDummy
import com.example.penjualan_produk_umkm.model.ItemPesanan
import com.example.penjualan_produk_umkm.model.MetodePembayaran
import com.example.penjualan_produk_umkm.model.Pesanan
import com.example.penjualan_produk_umkm.model.StatusPesanan
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.threeten.bp.LocalDate

class CheckoutFragment : Fragment() {

    private lateinit var cartAdapter: CartAdapter
    private var selectedShippingCost: Double = 0.0
    private var selectedPaymentMethod: MetodePembayaran = MetodePembayaran.CASH

    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserAddress: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserPhone = view.findViewById(R.id.tv_user_phone)
        tvUserAddress = view.findViewById(R.id.tv_user_address)

        val user = dummyUsers["user1@example.com"]!!
        tvUserName.text = user.nama
        tvUserPhone.text = user.noTelepon
        tvUserAddress.text = user.alamat

        view.findViewById<ImageButton>(R.id.btn_edit_address).setOnClickListener {
            showEditAddressDialog()
        }

        val cartItemsArray = arguments?.getParcelableArray("cartItems")
        val cartItems = cartItemsArray?.map { it as ItemPesanan } ?: emptyList()

        val rvProdukCheckout = view.findViewById<RecyclerView>(R.id.rv_produk_checkout)
        cartAdapter = CartAdapter(cartItems, isCheckout = true)
        rvProdukCheckout.layoutManager = LinearLayoutManager(requireContext())
        rvProdukCheckout.adapter = cartAdapter

        // Shipping Options
        val ekspedisiItems = ekspedisiDummy.map { "${it.nama} (${it.estimasiHari} hari) - Rp ${it.biaya}" }
        val ekspedisiAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ekspedisiItems)
        val actEkspedisi = view.findViewById<AutoCompleteTextView>(R.id.act_ekspedisi)
        actEkspedisi.setAdapter(ekspedisiAdapter)
        actEkspedisi.setOnItemClickListener { parent, _, position, _ ->
            selectedShippingCost = ekspedisiDummy[position].biaya
            updatePaymentDetails(view, cartItems, selectedShippingCost)
        }

        // Payment Methods
        val paymentMethodItems = MetodePembayaran.values().map { it.name }
        val paymentMethodAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, paymentMethodItems)
        val actMetodePembayaran = view.findViewById<AutoCompleteTextView>(R.id.act_metode_pembayaran)
        actMetodePembayaran.setAdapter(paymentMethodAdapter)
        actMetodePembayaran.setOnItemClickListener { _, _, position, _ ->
            selectedPaymentMethod = MetodePembayaran.values()[position]
        }

        // Initial state
        selectedShippingCost = ekspedisiDummy[0].biaya
        actEkspedisi.setText(ekspedisiItems[0], false)
        actMetodePembayaran.setText(paymentMethodItems[0], false)
        updatePaymentDetails(view, cartItems, selectedShippingCost)

        view.findViewById<MaterialButton>(R.id.btn_bayar).setOnClickListener {
            val newPesanan = Pesanan(
                id = (dummyPesanan.size + 1),
                user = user.copy( // Create a copy with the updated address
                    nama = tvUserName.text.toString(),
                    noTelepon = tvUserPhone.text.toString(),
                    alamat = tvUserAddress.text.toString()
                ),
                items = cartItems.toMutableList(),
                status = StatusPesanan.DIPROSES,
                ekspedisi = ekspedisiDummy.find { it.biaya == selectedShippingCost },
                tanggal = LocalDate.now(),
                metodePembayaran = selectedPaymentMethod
            )

            dummyPesanan.add(newPesanan)
            dummyItems.removeAll(cartItems.toSet())

            Toast.makeText(requireContext(), "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack(R.id.BerandaFragment, false)
        }
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
            val newAddress = etDialogAddress.text.toString()
            if (newAddress.isNotBlank()) {
                tvUserAddress.text = newAddress
                Toast.makeText(requireContext(), "Alamat berhasil diperbarui", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                etDialogAddress.error = "Alamat tidak boleh kosong"
            }
        }

        dialogView.findViewById<MaterialButton>(R.id.btn_dialog_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updatePaymentDetails(view: View, cartItems: List<ItemPesanan>, shippingCost: Double) {
        val subtotal = cartItems.sumOf { it.produk.harga * it.jumlah }
        val total = subtotal + shippingCost

        view.findViewById<TextView>(R.id.tv_subtotal_produk).text = "Rp ${String.format("%,.0f", subtotal)}"
        view.findViewById<TextView>(R.id.tv_biaya_pengiriman).text = "Rp ${String.format("%,.0f", shippingCost)}"
        view.findViewById<TextView>(R.id.tv_total_pembayaran).text = "Rp ${String.format("%,.0f", total)}"
    }
}
