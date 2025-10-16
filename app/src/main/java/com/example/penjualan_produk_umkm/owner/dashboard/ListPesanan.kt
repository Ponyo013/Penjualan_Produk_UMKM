package com.example.penjualan_produk_umkm.owner.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.penjualan_produk_umkm.dummyPesanan
import com.example.penjualan_produk_umkm.model.Pesanan
import com.example.penjualan_produk_umkm.model.StatusPesanan
import com.example.penjualan_produk_umkm.style.UMKMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPesanan(navController: NavController) {
    var selectedTab by remember { mutableStateOf("Baru") }

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
                    TabPesanan(
                        selectedTab = selectedTab,
                        onTabSelected = { tab -> selectedTab = tab }
                    )

                    // List Pesanan
                    CardPesananMasuk(selectedTab)
                }
            }
        }
    }
}

@Composable
fun CardPesananMasuk(selectedTab: String) {
    val filteredPesanan = when(selectedTab) {
        "Baru" -> dummyPesanan.filter { it.status == StatusPesanan.DIPROSES }
        "Diproses" -> dummyPesanan.filter { it.status == StatusPesanan.DIPROSES }
        "Selesai" -> dummyPesanan.filter { it.status == StatusPesanan.SELESAI }
        "Batal" -> dummyPesanan.filter { it.status == StatusPesanan.DIBATALKAN }
        "Dikirim" -> dummyPesanan.filter { it.status == StatusPesanan.DIKIRIM }
        else -> dummyPesanan
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(filteredPesanan) { pesanan ->
            CardPesanan(pesanan)
        }
    }
}

@Composable
fun CardPesanan(pesanan: Pesanan) {
    
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Header: Nama user + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = pesanan.user.nama + " (" + pesanan.user.noTelepon + ")", style = MaterialTheme.typography.titleMedium)
                    Text(text = pesanan.user.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (icon, bgColor) = when(pesanan.status) {
                        StatusPesanan.DIPROSES -> Icons.Filled.AccessTime to Color.Blue
                        StatusPesanan.DIKIRIM -> Icons.Outlined.LocalShipping to Color(0xFFFFA500) 
                        StatusPesanan.SELESAI -> Icons.Filled.CheckCircle to Color(0xFF9DC183)
                        StatusPesanan.DIBATALKAN -> Icons.Filled.Cancel to Color.Red
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(color = bgColor.copy(alpha = 0.2f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            tint = bgColor,
                            modifier = Modifier.size(20.dp),
                            contentDescription = pesanan.status.name
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: List Item Produk
            pesanan.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(item.produk.gambarUrl),
                        contentDescription = item.produk.nama,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                    )

                    Text(
                        text = item.produk.nama,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(text = "x${item.jumlah}")
                }
            }

            // Footer: Ekspedisi
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ekspedisi: ${pesanan.ekspedisi?.nama} (${pesanan.ekspedisi?.layanan}) - Est. ${pesanan.ekspedisi?.estimasiHari} hari",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}


@Composable
fun TabPesanan(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("Baru", "Diproses", "Dikirim" ,"Selesai", "Batal")

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs) { tab ->
            if (selectedTab == tab) {
                Button(
                    onClick = { onTabSelected(tab) },
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
                    onClick = { onTabSelected(tab)  },
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