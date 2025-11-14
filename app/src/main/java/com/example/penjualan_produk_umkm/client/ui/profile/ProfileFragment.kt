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
import androidx.lifecycle.lifecycleScope
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.auth.UserPreferences
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.model.User
import com.example.penjualan_produk_umkm.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var currentUser: User? = null
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentUser()

        binding.rlEditProfile.setOnClickListener { showEditAddressPopup() }
        binding.rlChangePassword.setOnClickListener { showChangePasswordPopup() }
        binding.rlLogout.setOnClickListener { showLogoutPopup() }
    }

    private fun loadCurrentUser() {
        val prefs = UserPreferences(requireContext())
        currentUserId = prefs.getUserId()

        if (currentUserId == -1) {
            redirectToLogin()
            return
        }

        db.userDao().getUserByIdLive(currentUserId).observe(viewLifecycleOwner) { user ->
            currentUser = user
            if (user != null) {
                binding.tvName.text = user.nama
                binding.tvEmail.text = user.email
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showEditAddressPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.popup_edit_address, null)
        val editTextAddress = dialogLayout.findViewById<EditText>(R.id.et_address)
        val tvNamePopup = dialogLayout.findViewById<TextView>(R.id.tv_name_popup)
        val tvEmailPopup = dialogLayout.findViewById<TextView>(R.id.tv_email_popup)
        val tvPhonePopup = dialogLayout.findViewById<TextView>(R.id.tv_phone_popup)

        currentUser?.let {
            editTextAddress.setText(it.alamat)
            tvNamePopup.text = it.nama
            tvEmailPopup.text = it.email
            tvPhonePopup.text = it.noTelepon
        }

        val dialog = builder.setView(dialogLayout).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogLayout.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val newAddress = editTextAddress.text.toString()

            if (newAddress.isNotEmpty() && currentUser != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    currentUser!!.alamat = newAddress
                    db.userDao().update(currentUser!!)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Alamat berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
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
        val dialogLayout = layoutInflater.inflate(R.layout.popup_change_password, null)

        val etOldPassword = dialogLayout.findViewById<EditText>(R.id.et_old_password)
        val etNewPassword = dialogLayout.findViewById<EditText>(R.id.et_new_password)
        val etConfirmNewPassword = dialogLayout.findViewById<EditText>(R.id.et_confirm_new_password)

        val dialog = builder.setView(dialogLayout).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogLayout.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val oldPassword = etOldPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmNewPassword.text.toString()

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (oldPassword != currentUser?.password) {
                Toast.makeText(requireContext(), "Kata sandi lama salah", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Kata sandi baru tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentUser?.let { user ->
                lifecycleScope.launch(Dispatchers.IO) {
                    user.password = newPassword
                    db.userDao().update(user)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Kata sandi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
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
            val prefs = UserPreferences(requireContext())
            prefs.clear()

            redirectToLogin()
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
