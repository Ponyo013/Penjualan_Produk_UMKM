package com.example.penjualan_produk_umkm.client.ui.checkout

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.client.ui.beranda.CartAdapter
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.MetodePembayaran
import com.example.penjualan_produk_umkm.viewModel.CheckoutViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.Executor

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private lateinit var cartAdapter: CartAdapter
    private var selectedShippingCost: Double = 0.0
    private var selectedPaymentMethod: MetodePembayaran = MetodePembayaran.CASH

    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserAddress: TextView

    private val viewModel: CheckoutViewModel by viewModels {
        ViewModelFactory()
    }

    private var cartItems: List<ItemPesanan> = emptyList()

    // Biometric components
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserPhone = view.findViewById(R.id.tv_user_phone)
        tvUserAddress = view.findViewById(R.id.tv_user_address)

        // Setup Biometric Authentication
        setupBiometric()

        // Observe user data
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                tvUserName.text = it.nama
                tvUserPhone.text = it.noTelepon
                tvUserAddress.text = it.alamat
            }
        }

        // Observe Error
        viewModel.error.observe(viewLifecycleOwner) { errMsg ->
            if (errMsg != null) {
                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe Loading State
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            val btnBayar = view.findViewById<MaterialButton>(R.id.btn_bayar)
            btnBayar.isEnabled = !isLoading
            btnBayar.text = if (isLoading) "Memproses..." else "Bayar"
        }

        // Setup RecyclerView
        setupRecyclerView(view)

        view.findViewById<ImageButton>(R.id.btn_edit_address).setOnClickListener {
            showEditAddressDialog()
        }

        // Shipping options
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

        // Payment methods
        val paymentMethodItems = MetodePembayaran.values().map { it.name }
        val actMetodePembayaran = view.findViewById<AutoCompleteTextView>(R.id.act_metode_pembayaran)
        val paymentMethodAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, paymentMethodItems)
        actMetodePembayaran.setAdapter(paymentMethodAdapter)

        actMetodePembayaran.setText(paymentMethodItems[0], false)

        actMetodePembayaran.setOnItemClickListener { _, _, position, _ ->
            selectedPaymentMethod = MetodePembayaran.values()[position]
        }

        view.findViewById<MaterialButton>(R.id.btn_bayar).setOnClickListener {
            validateAndProceedToCheckout(view)
        }
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(requireContext())

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        requireContext(),
                        "Autentikasi dibatalkan: $errString",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        requireContext(),
                        "Autentikasi berhasil! Memproses pembayaran...",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Proses pembayaran setelah autentikasi berhasil
                    view?.let { processPembayaran(it) }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        requireContext(),
                        "Autentikasi gagal. Silakan coba lagi.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verifikasi Pembayaran")
            .setSubtitle("Konfirmasi pembayaran Anda")
            .setDescription("Gunakan biometrik untuk menyelesaikan transaksi pembayaran")
            .setNegativeButtonText("Batal")
            .setConfirmationRequired(true)
            .build()
    }

    private fun setupRecyclerView(view: View) {
        val rvProdukCheckout = view.findViewById<RecyclerView>(R.id.rv_produk_checkout)

        viewModel.itemsWithProduk.observe(viewLifecycleOwner) { itemsWithProduk ->
            val selectedItems = itemsWithProduk.filter { it.isSelected }

            cartAdapter = CartAdapter(selectedItems, isCheckout = true)
            rvProdukCheckout.layoutManager = LinearLayoutManager(requireContext())
            rvProdukCheckout.adapter = cartAdapter

            cartItems = selectedItems
            updatePaymentDetails(view)
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
            val newName = tvDialogName.text.toString()
            val newPhone = tvDialogPhone.text.toString()
            val newAddress = etDialogAddress.text.toString()

            if (newName.isBlank()) {
                tvDialogName.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }
            if (newPhone.isBlank()) {
                tvDialogPhone.error = "No. Telepon tidak boleh kosong"
                return@setOnClickListener
            }
            if (newAddress.isBlank()) {
                etDialogAddress.error = "Alamat tidak boleh kosong"
                return@setOnClickListener
            }

            viewModel.updateUserAddress(newName, newPhone, newAddress)
            Toast.makeText(requireContext(), "Alamat berhasil diperbarui", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btn_dialog_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updatePaymentDetails(view: View) {
        val currentItems = viewModel.itemsWithProduk.value?.filter { it.isSelected } ?: emptyList()

        val subtotal = currentItems.sumOf { it.jumlah * it.produkHarga }
        val total = subtotal + selectedShippingCost

        view.findViewById<TextView>(R.id.tv_subtotal_produk).text = "Rp ${String.format("%,.0f", subtotal)}"
        view.findViewById<TextView>(R.id.tv_biaya_pengiriman).text = "Rp ${String.format("%,.0f", selectedShippingCost)}"
        view.findViewById<TextView>(R.id.tv_total_pembayaran).text = "Rp ${String.format("%,.0f", total)}"
    }

    private fun validateAndProceedToCheckout(view: View) {
        val user = viewModel.user.value

        when {
            user == null -> {
                Toast.makeText(requireContext(), "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
            user.alamat.isNullOrBlank() -> {
                Toast.makeText(requireContext(), "Harap lengkapi alamat pengiriman", Toast.LENGTH_SHORT).show()
            }
            cartItems.isEmpty() -> {
                Toast.makeText(requireContext(), "Keranjang kosong atau tidak ada barang dipilih!", Toast.LENGTH_SHORT).show()
            }
            selectedShippingCost <= 0 -> {
                Toast.makeText(requireContext(), "Mohon pilih ekspedisi", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Semua validasi OK, cek biometric availability
                checkBiometricAvailability(view)
            }
        }
    }

    private fun checkBiometricAvailability(view: View) {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Biometric tersedia, tampilkan prompt
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                    requireContext(),
                    "Perangkat tidak memiliki sensor biometrik",
                    Toast.LENGTH_SHORT
                ).show()
                // Fallback: tampilkan dialog konfirmasi manual
                showManualConfirmationDialog(view)
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(
                    requireContext(),
                    "Sensor biometrik tidak tersedia saat ini",
                    Toast.LENGTH_SHORT
                ).show()
                showManualConfirmationDialog(view)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    requireContext(),
                    "Tidak ada biometrik terdaftar. Silakan atur di pengaturan perangkat.",
                    Toast.LENGTH_LONG
                ).show()
                showManualConfirmationDialog(view)
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    "Autentikasi biometrik tidak didukung",
                    Toast.LENGTH_SHORT
                ).show()
                showManualConfirmationDialog(view)
            }
        }
    }

    private fun showManualConfirmationDialog(view: View) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Pembayaran")
            .setMessage("Biometrik tidak tersedia. Apakah Anda yakin ingin menyelesaikan pembayaran ini?")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Ya, Bayar") { _, _ ->
                processPembayaran(view)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun processPembayaran(view: View) {
        val selectedEkspedisiList = viewModel.ekspedisiAktif.value
        val selectedEkspedisi = selectedEkspedisiList?.find { it.biaya == selectedShippingCost }

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
}