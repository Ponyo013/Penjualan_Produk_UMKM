package com.example.penjualan_produk_umkm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.dao.ProdukDao
import com.example.penjualan_produk_umkm.database.dao.UserDao
import com.example.penjualan_produk_umkm.viewModel.*

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val userDao: UserDao? = null,
    private val produkDao: ProdukDao? = null,
    private val db: AppDatabase? = null,
    private val pesananId: Int? = null,
    private val userId: Int? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userDao!!) as T
            }

            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(userDao!!) as T
            }

            modelClass.isAssignableFrom(ProdukViewModel::class.java) -> {
                ProdukViewModel(produkDao!!) as T
            }

            modelClass.isAssignableFrom(CartViewModel::class.java) -> {
                CartViewModel(db!!, pesananId!!) as T
            }

            modelClass.isAssignableFrom(CheckoutViewModel::class.java) -> {
                CheckoutViewModel(db!!, userId!!) as T
            }

            modelClass.isAssignableFrom(UlasanViewModel::class.java) -> {
                UlasanViewModel(db!!.ulasanDao()) as T
            }

            modelClass.isAssignableFrom(PesananViewModel::class.java) -> {
                PesananViewModel(db!!, userId!!, db.itemPesananDao()) as T
            }

            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(db!!.pesananDao()) as T
            }

            modelClass.isAssignableFrom(EkspedisiViewModel::class.java) -> {
                EkspedisiViewModel(db!!.ekspedisiDao()) as T
            }

            modelClass.isAssignableFrom(OwnerPesananViewModel::class.java) -> {
                OwnerPesananViewModel(db!!.pesananDao(), db.itemPesananDao()) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
