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

class LoginViewModel(private val userDao: UserDao) : ViewModel() {

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val user = withContext(Dispatchers.IO) {
                    userDao.login(email, password)
                }
                if (user != null) {
                    _loginState.value = LoginState.Success(user)
                } else {
                    _loginState.value = LoginState.Error("Email atau password salah.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            callback(user)
        }
    }

}
