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
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.CurrentUser
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.FragmentProfileBinding
import com.example.penjualan_produk_umkm.dummyUsers

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = CurrentUser.user

        if (currentUser != null) {
            binding.tvName.text = currentUser.nama
            binding.tvEmail.text = currentUser.email
        }

        binding.rlEditProfile.setOnClickListener {
            showEditAddressPopup()
        }

        binding.rlChangePassword.setOnClickListener {
            showChangePasswordPopup()
        }

        binding.rlLogout.setOnClickListener {
            showLogoutPopup()
        }
    }

    private fun showEditAddressPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.popup_edit_address, null)
        val editTextAddress = dialogLayout.findViewById<EditText>(R.id.et_address)
        val tvNamePopup = dialogLayout.findViewById<TextView>(R.id.tv_name_popup)
        val tvEmailPopup = dialogLayout.findViewById<TextView>(R.id.tv_email_popup)
        val tvPhonePopup = dialogLayout.findViewById<TextView>(R.id.tv_phone_popup)

        CurrentUser.user?.let {
            editTextAddress.setText(it.alamat)
            tvNamePopup.text = it.nama
            tvEmailPopup.text = it.email
            tvPhonePopup.text = it.noTelepon
        }

        val dialog = builder.setView(dialogLayout).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogLayout.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val newAddress = editTextAddress.text.toString()
            if (newAddress.isNotEmpty()) {
                CurrentUser.user?.let {
                    it.alamat = newAddress
                    dummyUsers[it.email]?.alamat = newAddress
                }
                Toast.makeText(requireContext(), "Alamat berhasil diperbarui", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Alamat tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        dialogLayout.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showChangePasswordPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.popup_change_password, null)
        val etOldPassword = dialogLayout.findViewById<EditText>(R.id.et_old_password)
        val etNewPassword = dialogLayout.findViewById<EditText>(R.id.et_new_password)
        val etConfirmNewPassword = dialogLayout.findViewById<EditText>(R.id.et_confirm_new_password)

        val dialog = builder.setView(dialogLayout).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogLayout.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val oldPassword = etOldPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmNewPassword = etConfirmNewPassword.text.toString()

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (oldPassword != CurrentUser.user?.password) {
                Toast.makeText(requireContext(), "Kata sandi lama salah", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmNewPassword) {
                Toast.makeText(requireContext(), "Kata sandi baru tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CurrentUser.user?.let {
                it.password = newPassword
                dummyUsers[it.email]?.password = newPassword
            }

            Toast.makeText(requireContext(), "Kata sandi berhasil diperbarui", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogLayout.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLogoutPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.popup_logout, null)

        val dialog = builder.setView(dialogLayout).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogLayout.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            CurrentUser.user = null
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