package com.example.penjualan_produk_umkm.client.ui.profile

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.auth.UserPreferences
import com.example.penjualan_produk_umkm.database.firestore.model.User
import com.example.penjualan_produk_umkm.databinding.FragmentProfileBinding
import com.example.penjualan_produk_umkm.viewModel.ProfileViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory()
    }

    // Variabel untuk Lokasi
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var etAddressReference: TextInputEditText? = null // Referensi ke EditText Alamat di Dialog

    // Permission Launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocation || coarseLocation) {
                getCurrentLocation()
            } else {
                Toast.makeText(context, "Izin lokasi diperlukan untuk fitur ini", Toast.LENGTH_SHORT).show()
            }
        }

    private var currentUserData: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Load data awal
        viewModel.loadUserProfile()

        // Observasi Data User
        viewModel.user.observe(viewLifecycleOwner) { user ->
            currentUserData = user
            if (user != null) {
                binding.tvName.text = user.nama
                binding.tvEmail.text = user.email
                // Anda bisa menambahkan logic load gambar profil di sini jika ada
            }
        }

        // Observasi Status Update
        viewModel.updateStatus.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.resetStatus()
            }
        }

        // Setup Listeners
        binding.rlEditProfile.setOnClickListener {
            // Pastikan data user sudah ada sebelum membuka dialog
            currentUserData?.let { user ->
                showEditProfileDialog(user.nama, user.noTelepon, user.alamat)
            } ?: run {
                Toast.makeText(context, "Memuat data user...", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rlChangePassword.setOnClickListener { showChangePasswordPopup() }

        binding.rlLogout.setOnClickListener { showLogoutPopup() }
    }

    // --- FUNGSI 1: EDIT PROFIL & LOKASI ---
    private fun showEditProfileDialog(currentName: String, currentPhone: String, currentAddress: String) {
        // Inflate layout dialog_edit_address.xml
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_address, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Binding View di Dialog
        val etName = dialogView.findViewById<TextInputEditText>(R.id.tv_dialog_name)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.tv_dialog_phone)
        val etAddress = dialogView.findViewById<TextInputEditText>(R.id.et_dialog_address)
        val tilAddress = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.til_dialog_address)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_save)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_cancel)

        // Simpan referensi EditText Alamat agar bisa diakses oleh fungsi lokasi
        etAddressReference = etAddress

        // Isi data lama
        etName.setText(currentName)
        etPhone.setText(currentPhone)
        etAddress.setText(currentAddress)

        // SET LISTENER ON THE END ICON
        tilAddress.setEndIconOnClickListener {
            checkLocationPermissionAndGet()
        }

        // Listener Simpan
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()
            val newAddress = etAddress.text.toString().trim()

            if (newName.isBlank() || newPhone.isBlank() || newAddress.isBlank()) {
                Toast.makeText(context, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.updateUserProfile(newName, newPhone, newAddress)
                dialog.dismiss()
            }
        }

        // Listener Batal
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // --- LOGIKA PERMISSION & GET LOCATION ---
    private fun checkLocationPermissionAndGet() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Minta Izin
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        // Jika izin sudah ada, ambil lokasi
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        Toast.makeText(context, "Mencari lokasi...", Toast.LENGTH_SHORT).show()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                getAddressFromCoordinate(location.latitude, location.longitude)
            } else {
                Toast.makeText(context, "Gagal mendapatkan lokasi. Pastikan GPS aktif.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddressFromCoordinate(lat: Double, lon: Double) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale("id", "ID"))
                // Mengambil max 1 hasil
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val addressObj = addresses[0]
                        val fullAddress = StringBuilder()

                        val street = addressObj.thoroughfare ?: ""
                        val subLocality = addressObj.subLocality
                        val locality = addressObj.locality
                        val subAdmin = addressObj.subAdminArea
                        val admin = addressObj.adminArea
                        val postalCode = addressObj.postalCode ?: ""

                        if (street.isNotEmpty()) fullAddress.append("$street, ")
                        if (subLocality != null) fullAddress.append("$subLocality, ")
                        if (locality != null) fullAddress.append("$locality, ")
                        if (subAdmin != null) fullAddress.append("$subAdmin, ")
                        if (admin != null) fullAddress.append(admin)
                        if (postalCode.isNotEmpty()) fullAddress.append(" $postalCode")

                        val finalAddress = if (fullAddress.length < 5) addressObj.getAddressLine(0) else fullAddress.toString()

                        // Set teks ke EditText di Dialog
                        etAddressReference?.setText(finalAddress)
                        Toast.makeText(context, "Lokasi ditemukan!", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(context, "Alamat tidak ditemukan untuk koordinat ini", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal memuat alamat: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- FUNGSI 2: GANTI PASSWORD ---
    private fun showChangePasswordPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.popup_change_password, null)
        val dialog = builder.setView(dialogLayout).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etOldPass = dialogLayout.findViewById<EditText>(R.id.et_old_password)
        val etNewPass = dialogLayout.findViewById<EditText>(R.id.et_new_password)
        val etConfirmPass = dialogLayout.findViewById<EditText>(R.id.et_confirm_new_password)
        val btnSave = dialogLayout.findViewById<Button>(R.id.btn_save)
        val btnCancel = dialogLayout.findViewById<Button>(R.id.btn_cancel)

        btnSave.setOnClickListener {
            val oldPass = etOldPass.text.toString()
            val newPass = etNewPass.text.toString()
            val confirmPass = etConfirmPass.text.toString()

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(requireContext(), "Password baru tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(requireContext(), "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.changePassword(oldPass, newPass,
                onSuccess = {
                    Toast.makeText(requireContext(), "Password berhasil diubah!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                },
                onError = { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            )
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // --- FUNGSI 3: LOGOUT ---
    private fun showLogoutPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.popup_logout, null)
        val dialog = builder.setView(dialogLayout).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogLayout.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            viewModel.logout()
            val prefs = UserPreferences(requireContext())
            prefs.clear()

            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            dialog.dismiss()
        }

        dialogLayout.findViewById<Button>(R.id.btn_no).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        etAddressReference = null // Hindari memory leak
    }
}