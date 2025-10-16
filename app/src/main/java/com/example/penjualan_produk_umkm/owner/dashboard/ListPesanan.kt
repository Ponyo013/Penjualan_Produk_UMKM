package com.example.penjualan_produk_umkm.owner.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class Pesanan(val id: Int, val namaPembeli: String, val total: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPesanan(navController: NavController) {
    // Contoh data dummy
    val pesananList = listOf(
        Pesanan(1, "Budi", 150000.0),
        Pesanan(2, "Siti", 200000.0),
        Pesanan(3, "Andi", 120000.0)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pesanan yang masuk") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            })
        },
        ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(pesananList) { pesanan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Nama: ${pesanan.namaPembeli}", style = MaterialTheme.typography.bodyLarge)
                            Text("Total: Rp${pesanan.total}", style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = {
                            // Bisa navigasi ke detail pesanan
                            // navController.navigate("detailPesanan/${pesanan.id}")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Lihat Detail"
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewListPesanan() {
    // Dummy NavController untuk preview
    val navController = rememberNavController()
    ListPesanan(navController = navController)
}