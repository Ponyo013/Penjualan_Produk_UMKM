package com.example.penjualan_produk_umkm

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.penjualan_produk_umkm.owner.dashboard.AddProdukScreen
import com.example.penjualan_produk_umkm.owner.dashboard.DashboardScreen
import com.example.penjualan_produk_umkm.owner.dashboard.EditProdukScreen
import com.example.penjualan_produk_umkm.owner.dashboard.Keuangan
import com.example.penjualan_produk_umkm.owner.dashboard.ListPesanan
import com.example.penjualan_produk_umkm.owner.dashboard.ProdukManage

class OwnerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "dashboard") {
                        // Dashboard
                        composable("dashboard") { DashboardScreen(navController) }

                        // CRUD
                        composable("produkManage") { ProdukManage(navController) }
                        composable("addProduk") { AddProdukScreen(navController) }
                        composable("edit_produk/{produkId}") { backStackEntry ->
                            val produkId = backStackEntry.arguments?.getString("produkId")?.toIntOrNull()
                            val produk = produkDummyList.find { it.id == produkId }
                            if (produk != null) {
                                EditProdukScreen(
                                    produk = produk,
                                    onSave = { updated ->
                                        val index = produkDummyList.indexOfFirst { it.id == produk.id }
                                        if (index != -1) {
                                            produkDummyList[index] = updated
                                        }
                                    },
                                    onCancel = { navController.popBackStack() },
                                    navController = navController
                                )
                            }
                        }

                        // List Status Pesanan
                        composable("listPesanan") { ListPesanan(navController) }

                        // Laporan Keuangan
                        composable("Keuangan") { Keuangan(navController) }
                    }
                }
        }


    }
}