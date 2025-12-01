package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _updateStatus = MutableLiveData<String?>()
    val updateStatus: LiveData<String?> = _updateStatus

    // Ambil data user dari Firebase saat fragment dibuka
    fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        // Mapping manual agar aman
                        val user = User(
                            id = 0, // Dummy ID untuk lokal
                            nama = document.getString("nama") ?: "",
                            email = document.getString("email") ?: "",
                            password = "",
                            role = document.getString("role") ?: "user",
                            noTelepon = document.getString("noTelepon") ?: "",
                            alamat = document.getString("alamat") ?: ""
                        )
                        _user.value = user
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    // Update data ke Firebase
    fun updateUserProfile(name: String, phone: String, address: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _updateStatus.value = "User tidak ditemukan"
            return
        }

        val updates = mapOf(
            "nama" to name,
            "noTelepon" to phone,
            "alamat" to address
        )

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                _updateStatus.value = "Profil berhasil diperbarui"
                loadUserProfile() // Refresh data di UI
            }
            .addOnFailureListener {
                _updateStatus.value = "Gagal memperbarui profil: ${it.message}"
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun resetStatus() {
        _updateStatus.value = null
    }
}