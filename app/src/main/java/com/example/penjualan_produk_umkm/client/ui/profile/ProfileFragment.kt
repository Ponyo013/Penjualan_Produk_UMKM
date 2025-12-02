package com.example.penjualan_produk_umkm.client.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.auth.UserPreferences
import com.example.penjualan_produk_umkm.database.firestore.model.User
import com.example.penjualan_produk_umkm.databinding.FragmentProfileBinding
import com.example.penjualan_produk_umkm.viewModel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Gunakan ViewModel Firebase
    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory()
    }

    // Variabel ini sekarang menggunakan tipe User dari Firestore
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

        // Load data awal
        viewModel.loadUserProfile()

        // Observasi Data User dari Firebase
        viewModel.user.observe(viewLifecycleOwner) { user ->
            // Sekarang tipe datanya cocok (Firestore User -> Firestore User)
            currentUserData = user
            if (user != null) {
                binding.tvName.text = user.nama
                binding.tvEmail.text = user.email
            }
        }

        // Observasi Status Update (Toast)
        viewModel.updateStatus.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.resetStatus()
            }
        }

        binding.rlEditProfile.setOnClickListener { showEditProfilePopup() }
        binding.rlLogout.setOnClickListener { showLogoutPopup() }
    }

    private fun showEditProfilePopup() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.popup_edit_address, null)

        // Inisialisasi View dari Popup
        val etName = dialogLayout.findViewById<EditText>(R.id.et_name_popup)
        // Email tetap TextView karena ReadOnly
        val tvEmail = dialogLayout.findViewById<TextView>(R.id.tv_email_popup)
        val etPhone = dialogLayout.findViewById<EditText>(R.id.et_phone_popup)
        val etAddress = dialogLayout.findViewById<EditText>(R.id.et_address_popup)

        // Isi data lama ke form
        currentUserData?.let {
            etName.setText(it.nama)
            tvEmail.text = it.email
            etPhone.setText(it.noTelepon)
            etAddress.setText(it.alamat)
        }

        val dialog = builder.setView(dialogLayout).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogLayout.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val newName = etName.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()
            val newAddress = etAddress.text.toString().trim()

            if (newName.isEmpty() || newPhone.isEmpty() || newAddress.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show()
            } else {
                // Panggil ViewModel untuk update ke Firebase
                viewModel.updateUserProfile(newName, newPhone, newAddress)
                dialog.dismiss()
            }
        }

        dialogLayout.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLogoutPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.popup_logout, null)
        val dialog = builder.setView(dialogLayout).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogLayout.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            // Logout Firebase & Lokal
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
    }
}