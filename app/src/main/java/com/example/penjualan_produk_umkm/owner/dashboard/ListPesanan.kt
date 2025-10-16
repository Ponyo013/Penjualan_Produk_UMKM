package com.example.penjualan_produk_umkm.owner.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.penjualan_produk_umkm.dummyPesanan
import com.example.penjualan_produk_umkm.style.UMKMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPesanan(navController: NavController) {

    UMKMTheme {
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tabs
                    TabPesanan()

                    // List Pesanan

                }
            }
        }
    }
}

@Composable
fun ListPesanan() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dummyPesanan) { pesanan ->
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Bagian Produk
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Gambar produk pertama
                        Image(
                            painter = rememberAsyncImagePainter(pesanan.items.first().produk.gambarUrl),
                            contentDescription = pesanan.items.first().produk.nama,
                            modifier = Modifier
                                .size(80.dp)
                                .padding(end = 12.dp),
                            contentScale = ContentScale.Fit
                        )

                        // Detail produk
                        Column {
                            pesanan.items.forEach { item ->
                                Text(
                                    text = item.produk.nama,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("Harga: Rp ${item.produk.harga}")
                                Text("Jumlah: ${item.jumlah}")
                                Text("Subtotal: Rp ${item.subtotal}")
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        Text("Pembeli: ${pesanan.user.nama}", fontWeight = FontWeight.Medium)
                        Text("Ekspedisi: JNE / SiCepat / POS", fontWeight = FontWeight.Medium)
                        Text("Alamat: Jalan Contoh No.123, Jakarta", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun TabPesanan() {
    var selectedTab by remember { mutableStateOf("Baru") }
    val tabs = listOf("Baru", "Diproses", "Selesai", "Batal")

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs) { tab ->
            if (selectedTab == tab) {
                Button(
                    onClick = { selectedTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(tab)
                }
            } else {
                OutlinedButton(
                    onClick = { selectedTab = tab },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(tab)
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