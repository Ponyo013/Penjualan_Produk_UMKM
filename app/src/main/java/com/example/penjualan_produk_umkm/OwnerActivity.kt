package com.example.penjualan_produk_umkm

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.owner.dashboard.*
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.model.Pesanan
import com.example.penjualan_produk_umkm.viewModel.EkspedisiViewModel
import com.example.penjualan_produk_umkm.viewModel.OwnerPesananViewModel
import com.example.penjualan_produk_umkm.viewModel.UlasanViewModel

class OwnerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            UMKMTheme {
                val navController = rememberNavController()
                val db = AppDatabase.getDatabase(this)

                val produkViewModel: ProdukViewModel = viewModel(
                    factory = ViewModelFactory(
                        produkDao = db.produkDao()
                    )
                )
                val ulasanViewModel: UlasanViewModel = viewModel(
                    factory = ViewModelFactory(
                        db = db
                    )
                )
                val ownerProdukViewModel: OwnerPesananViewModel = viewModel(
                    factory = ViewModelFactory(
                        db = db
                    )
                )
                val expedisiViewModel: EkspedisiViewModel = viewModel(
                    factory = ViewModelFactory(
                        db = db
                    )
                )

                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "dashboard") {
                        // Masing Masing page
                        composable("dashboard") { DashboardScreen(navController) }
                        composable("produkManage") {
                            ProdukManage(navController, produkViewModel)
                        }
                        composable("ulasan/{produkId}") { backStackEntry ->
                            val produkId = backStackEntry.arguments?.getString("produkId")?.toInt() ?: 0

                            UlasanScreen(
                                produkId = produkId,
                                navController = navController,
                                ulasanViewModel = ulasanViewModel
                            )

                        }
                        composable("keuangan") {
                            Keuangan(navController, ownerProdukViewModel)
                        }
                        composable("listpesanan") {
                            ListPesanan(navController, ownerProdukViewModel)
                        }
                        composable("kirimNotifikasi") {
                            KirimNotifikasiScreen(navController)
                        }
                        composable("expedisi") {
                            ExpedisiScreen(navController, expedisiViewModel)
                        }

                        // CRUD Produk
                        composable("addProduk") {
                            AddProdukScreen(navController, produkViewModel)
                        }
                        composable("edit_produk/{produkId}") { backStackEntry ->
                            val produkId = backStackEntry.arguments?.getString("produkId")?.toIntOrNull()
                            val produk by produkId?.let { produkViewModel.getProdukById(it).observeAsState() } ?: return@composable

                            produk?.let { nonNullProduk ->
                                EditProdukScreen(
                                    produk = nonNullProduk,
                                    onCancel = { navController.popBackStack() },
                                    navController = navController,
                                    produkViewModel = produkViewModel
                                )
                            }
                        }


                    }
                }
            }
        }
    }
}
