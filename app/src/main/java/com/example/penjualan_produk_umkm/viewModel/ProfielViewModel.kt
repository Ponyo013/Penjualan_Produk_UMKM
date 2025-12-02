package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
// PENTING: Pastikan import ini mengarah ke Model Firestore, BUKAN database.model (Room)
import com.example.penjualan_produk_umkm.database.firestore.model.User
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
                        // Mengambil data dan memasukkannya ke object User
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            user.id = userId // Pastikan ID terisi
                            _user.value = user
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _updateStatus.value = "Gagal memuat profil: ${e.message}"
                    }
                }
            }
            .addOnFailureListener {
                _updateStatus.value = "Gagal mengambil data: ${it.message}"
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
                loadUserProfile() // Refresh data di UI agar langsung berubah
            }
            .addOnFailureListener {
                _updateStatus.value = "Gagal memperbarui profil: ${it.message}"
            }
    }

    // Fungsi Ganti Password
    fun changePassword(currentPassword: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        val email = user?.email

        if (user != null && email != null) {
            // Re-authenticate dulu (Wajib untuk operasi sensitif seperti ganti password)
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)

            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPassword)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError("Gagal update password: ${e.message}") }
                }
                .addOnFailureListener {
                    onError("Password lama salah")
                }
        } else {
            onError("User tidak valid")
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun resetStatus() {
        _updateStatus.value = null
    }
}