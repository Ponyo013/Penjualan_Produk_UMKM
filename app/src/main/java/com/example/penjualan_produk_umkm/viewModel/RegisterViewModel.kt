package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterViewModel : ViewModel() {

    // State untuk UI
    sealed class RegisterState {
        object Loading : RegisterState()
        data class Success(val message: String) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    // Inisialisasi Firebase
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun register(
        name: String,
        email: String,
        password: String,
        noTelepon: String = ""
    ) {
        _registerState.value = RegisterState.Loading

        // 1. Buat Akun di Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // Akun berhasil dibuat, sekarang ambil UID (User ID unik dari Firebase)
                val userId = authResult.user?.uid

                if (userId != null) {
                    // 2. Tentukan Role (Logika Admin Otomatis)
                    val roleOtomatis = if (email == "admin@dwiusaha.com") "owner" else "user"

                    // 3. Siapkan data untuk disimpan ke Firestore
                    val userData = hashMapOf(
                        "id" to userId,
                        "nama" to name,
                        "email" to email,
                        "role" to roleOtomatis,
                        "noTelepon" to noTelepon,
                        "alamat" to "" // Default kosong
                    )

                    // 4. Simpan data detail user ke Firestore -> Collection "users"
                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            _registerState.value = RegisterState.Success("Registrasi berhasil!")
                            // Logout agar user harus login ulang (opsional, best practice)
                            auth.signOut()
                        }
                        .addOnFailureListener { e ->
                            // Gagal simpan ke database, tapi akun Auth sudah terbuat.
                            // Idealnya akun Auth dihapus lagi, tapi untuk simpel biarkan saja.
                            _registerState.value = RegisterState.Error("Gagal menyimpan data user: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                // Gagal membuat akun (misal: email duplikat, password lemah, sinyal jelek)
                _registerState.value = RegisterState.Error("Gagal Register: ${e.message}")
            }
    }
}