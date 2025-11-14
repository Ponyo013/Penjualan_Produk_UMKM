package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.dao.UserDao
import com.example.penjualan_produk_umkm.database.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel(private val userDao: UserDao) : ViewModel() {

    sealed class RegisterState {
        object Loading : RegisterState()
        data class Success(val message: String) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    fun register(
        name: String,
        email: String,
        password: String,
        noTelepon: String = ""
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val existingUser = withContext(Dispatchers.IO) {

                    // Cek apakah user sudah terdaftar, baik lewat email atau no telepon
                    if (email.isNotEmpty()) {
                        userDao.getUserByEmail(email)
                    } else if (noTelepon.isNotEmpty()) {
                        userDao.getUserByNoTelepon(noTelepon)
                    } else null
                }

                if (existingUser != null) {
                    _registerState.value = RegisterState.Error("Akun sudah terdaftar.")
                } else {
                    val newUser = User(
                        nama = name,
                        email = email,
                        password = password,
                        role = "user",
                        noTelepon = noTelepon
                    )

                    withContext(Dispatchers.IO) {
                        userDao.insert(newUser)
                    }
                    _registerState.value = RegisterState.Success("Registrasi berhasil!")
                }

            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }
}
