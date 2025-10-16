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
                        composable("dashboard") { DashboardScreen(navController) }
                        composable("produkManage") { ProdukManage(navController) }
                        composable("addProduk") { AddProdukScreen(navController) }
                    }
                }
        }


    }
}