package com.example.penjualan_produk_umkm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.penjualan_produk_umkm.viewModel.*

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val userId: String? = null,
    private val pesananId: String? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel() as T
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel() as T
            modelClass.isAssignableFrom(ProdukViewModel::class.java) -> ProdukViewModel() as T
            modelClass.isAssignableFrom(CartViewModel::class.java) -> CartViewModel() as T
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) -> CheckoutViewModel() as T
            modelClass.isAssignableFrom(UlasanViewModel::class.java) -> UlasanViewModel() as T

//            modelClass.isAssignableFrom(PesananViewModel::class.java) -> {
//                if (userId == null) throw IllegalArgumentException("userId required for PesananViewModel")
//                PesananViewModel(userId) as T
//            }

//            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel() as T
//            modelClass.isAssignableFrom(EkspedisiViewModel::class.java) -> EkspedisiViewModel() as T
//            modelClass.isAssignableFrom(OwnerPesananViewModel::class.java) -> OwnerPesananViewModel() as T
//            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel() as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
