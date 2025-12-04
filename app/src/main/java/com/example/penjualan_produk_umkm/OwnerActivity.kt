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
                // Factory Kosong untuk ViewModel Firebase
                val factory = ViewModelFactory()
                val produkViewModel: ProdukViewModel = viewModel(
                    factory = ViewModelFactory()
                )
                val ulasanViewModel: UlasanViewModel = viewModel(
                    factory = ViewModelFactory()
                )
                val ownerProdukViewModel: OwnerPesananViewModel = viewModel(
                    factory = ViewModelFactory()
                )
                val expedisiViewModel: EkspedisiViewModel = viewModel(
                    factory = ViewModelFactory()
                )

                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "dashboard") {

                        // --- HALAMAN UTAMA ---
                        composable("dashboard") { DashboardScreen(navController) }

                        composable("produkManage") {
                            ProdukManage(navController, produkViewModel)
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

                        // --- HALAMAN CRUD & DETAIL ---

                        // Halaman Ulasan (ID String)
                        composable("ulasan/{produkId}") { backStackEntry ->
                            // FIX: Ambil sebagai String, bukan Int
                            val produkId = backStackEntry.arguments?.getString("produkId") ?: ""

                            UlasanScreen(
                                produkId = produkId,
                                navController = navController,
                                ulasanViewModel = ulasanViewModel
                            )
                        }

                        // Halaman Tambah Produk
                        composable("addProduk") {
                            AddProdukScreen(navController, produkViewModel)
                        }


                        // Halaman Edit Produk (ID String + Async Fetch)
                        composable("edit_produk/{produkId}") { backStackEntry ->
                            // 1. Ambil ID sebagai String
                            val produkId = backStackEntry.arguments?.getString("produkId") ?: ""

                            // 2. State lokal untuk menampung data produk (Tipe: Produk Firestore)
                            var produk by remember { mutableStateOf<Produk?>(null) }

                            // 3. Ambil data dari Firebase
                            LaunchedEffect(produkId) {
                                if (produkId.isNotEmpty()) {
                                    // Panggil fungsi di ViewModel
                                    produkViewModel.getProdukById(produkId) { result ->
                                        // result bertipe Produk?
                                        produk = result
                                    }
                                }
                            }

                            // 4. Tampilkan layar edit
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