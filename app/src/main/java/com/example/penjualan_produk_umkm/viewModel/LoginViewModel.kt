package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.threeten.bp.LocalDate

class LoginViewModel : ViewModel() { // Hapus parameter UserDao

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
        object Idle : LoginState()
    }

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun login(email: String, pass: String) {
        _loginState.value = LoginState.Loading

        // 1. Login ke Firebase Authentication
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid

                if (userId != null) {
                    // 2. Ambil detail user (Role, Nama, dll) dari Firestore
                    fetchUserDetails(userId)
                } else {
                    _loginState.value = LoginState.Error("Gagal mendapatkan ID User")
                }
            }
            .addOnFailureListener { e ->
                // Handle error (Password salah / User tidak ditemukan)
                _loginState.value = LoginState.Error("Login Gagal: ${e.message}")
            }
    }

    private fun fetchUserDetails(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        // Mapping manual dari Firestore ke Object User Lokal
                        // Catatan: ID di Firebase itu String (UID), tapi di Room local kita Int.
                        // Untuk sementara kita pakai hashCode() agar bisa masuk ke objek User,
                        // tapi idealnya nanti semua diganti String.

                        val role = document.getString("role") ?: "user"
                        val nama = document.getString("nama") ?: "User"
                        val email = document.getString("email") ?: ""
                        val noTelepon = document.getString("noTelepon") ?: ""
                        val alamat = document.getString("alamat") ?: ""

                        val user = User(
                            id = uid.hashCode(),
                            nama = nama,
                            email = email,
                            password = "",
                            role = role,
                            noTelepon = noTelepon,
                            alamat = alamat,
                            tanggal = LocalDate.now()
                        )

                        _loginState.value = LoginState.Success(user)
                    } catch (e: Exception) {
                        _loginState.value = LoginState.Error("Error parsing data: ${e.message}")
                    }
                } else {
                    _loginState.value = LoginState.Error("Data user tidak ditemukan di database")
                }
            }
            .addOnFailureListener { e ->
                _loginState.value = LoginState.Error("Gagal mengambil data: ${e.message}")
            }
    }
}