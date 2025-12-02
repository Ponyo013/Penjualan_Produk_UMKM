package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
// PENTING: Gunakan Import Firestore Model
import com.example.penjualan_produk_umkm.database.firestore.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {

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
                    // 2. Ambil detail user dari Firestore
                    fetchUserDetails(userId)
                } else {
                    _loginState.value = LoginState.Error("Gagal mendapatkan ID User")
                }
            }
            .addOnFailureListener { e ->
                _loginState.value = LoginState.Error("Login Gagal: ${e.message}")
            }
    }

    private fun fetchUserDetails(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        // CARA BERSIH: Convert langsung dokumen ke Object User
                        // Pastikan class User di folder firestore.model sudah benar (var id: String)
                        val user = document.toObject(User::class.java)

                        if (user != null) {
                            // Pastikan ID terisi dengan UID dari Auth
                            user.id = uid
                            _loginState.value = LoginState.Success(user)
                        } else {
                            _loginState.value = LoginState.Error("Gagal memparsing data user")
                        }

                    } catch (e: Exception) {
                        _loginState.value = LoginState.Error("Error: ${e.message}")
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