package com.example.penjualan_produk_umkm

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.penjualan_produk_umkm.owner.dashboard.*
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import com.example.penjualan_produk_umkm.database.firestore.model.Produk

class OwnerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            UMKMTheme {
                val navController = rememberNavController()

                val produkViewModel: ProdukViewModel = viewModel()

                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "dashboard") {
                        // Masing Masing page
                        composable("dashboard") { DashboardScreen(navController) }
                        composable("produkManage") {
                            ProdukManage(navController, produkViewModel)
                        }

                        // CRUD Produk
                        composable("addProduk") {
                            AddProdukScreen(navController, produkViewModel)
                        }

                        composable("edit_produk/{produkId}") { backStackEntry ->
                            val produkId = backStackEntry.arguments?.getString("produkId") ?: ""

                            var produk by remember { mutableStateOf<Produk?>(null) }

                            // Panggil Firestore sekali saat screen dibuka
                            LaunchedEffect(produkId) {
                                produkViewModel.getProdukById(produkId) { result ->
                                    produk = result
                                }
                            }

                            // Jika produk sudah berhasil di-load, tampilkan screen edit
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
