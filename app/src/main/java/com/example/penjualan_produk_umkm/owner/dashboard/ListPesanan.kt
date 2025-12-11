package com.example.penjualan_produk_umkm.owner.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.OwnerPesananViewModel
import com.example.penjualan_produk_umkm.viewModel.PesananLengkap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPesanan(navController: NavController, viewModel: OwnerPesananViewModel) {
    var selectedTab by remember { mutableStateOf("Baru") }
    val pesananList by viewModel.pesananList.collectAsState()

    val filteredPesananList = remember(selectedTab, pesananList) {
        val now = System.currentTimeMillis()
        val duaJam = 2 * 60 * 60 * 1000L
        pesananList.filter { p ->
            when (selectedTab) {
                "Baru" -> now - p.pesanan.tanggal.toDate().time <= duaJam && p.pesanan.status == StatusPesanan.DIPROSES.name
                "Diproses" -> p.pesanan.status == StatusPesanan.DIPROSES.name
                "Dikirim" -> p.pesanan.status == StatusPesanan.DIKIRIM.name
                "Selesai" -> p.pesanan.status == StatusPesanan.SELESAI.name
                "Batal" -> p.pesanan.status == StatusPesanan.DIBATALKAN.name
                else -> true
            }
        }
    }


    UMKMTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Pesanan Masuk") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)) {
                TabPesanan(selectedTab) { selectedTab = it }

                // Filter Tab baru (< 2 jam) dan proses (> 2 jam)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredPesananList) { pesananLengkap ->
                        CardPesanan(
                            pesananLengkap = pesananLengkap,
                            onStatusChange = { newStatus ->
                                viewModel.updateStatus(pesananLengkap.pesanan.id, newStatus)

                                // pindah tab otomatis ke status baru
                                selectedTab = when (newStatus) {
                                    StatusPesanan.DIPROSES -> "Diproses"
                                    StatusPesanan.DIKIRIM -> "Dikirim"
                                    StatusPesanan.SELESAI -> "Selesai"
                                    StatusPesanan.DIBATALKAN -> "Batal"
                                    else -> "Baru"
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardPesanan(pesananLengkap: PesananLengkap, onStatusChange: (StatusPesanan) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    // Convert String status dari Firestore ke Enum agar aman untuk UI Logic
    val currentStatusEnum = try {
        StatusPesanan.valueOf(pesananLengkap.pesanan.status)
    } catch (e: Exception) {
        StatusPesanan.DIPROSES
    }
    var selectedStatus by remember { mutableStateOf(currentStatusEnum) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = pesananLengkap.user?.nama ?: "User tidak diketahui",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = pesananLengkap.user?.email ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                val (icon, bgColor) = when (currentStatusEnum) {
                    StatusPesanan.DIPROSES -> Icons.Filled.AccessTime to Color.Blue
                    StatusPesanan.DIKIRIM -> Icons.Outlined.LocalShipping to Color(0xFFFFA500)
                    StatusPesanan.SELESAI -> Icons.Filled.CheckCircle to Color(0xFF9DC183)
                    StatusPesanan.DIBATALKAN -> Icons.Filled.Cancel to Color.Red
                    else -> Icons.Filled.Info to Color.Gray
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
                        contentDescription = currentStatusEnum.name
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            pesananLengkap.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gambar
                    AsyncImage(
                        model = item.gambarUrl.ifEmpty { com.example.penjualan_produk_umkm.R.drawable.ic_error_image },
                        contentDescription = item.produkNama,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .weight(1f),
                        contentScale = ContentScale.Fit,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Nama Produk
                    Text(
                        text = item.produkNama,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(3f)
                    )

                    // Jumlah
                    Text(
                        text = "x${item.jumlah}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Alamat: ${pesananLengkap.user?.alamat ?: "-"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))
            // Ekspedisi mungkin null jika data belum lengkap, handle safely
            Text(
                text = "Ekspedisi: - ${pesananLengkap.ekspedisi?.nama ?: "-"}", // Placeholder karena data ekspedisi tidak di-join di ViewModel saat ini
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Text("Ubah Status Pesanan")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Text(
                            "Edit Status Pesanan",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        var expanded by remember { mutableStateOf(false) }
                        Column {
                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedStatus.name)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    StatusPesanan.values()
                                        .filter { it.name != "KERANJANG" }
                                        .forEach { status ->
                                            DropdownMenuItem(
                                                text = { Text(status.name) },
                                                onClick = {
                                                    selectedStatus = status
                                                    expanded = false
                                                }
                                            )
                                        }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onStatusChange(selectedStatus)
                            showDialog = false
                        }) { Text("Simpan") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Batal") }
                    }
                )
            }
        }
    }
}

@Composable
fun TabPesanan(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("Baru", "Diproses", "Dikirim", "Selesai", "Batal")

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                ) { Text(tab) }
            } else {
                OutlinedButton(
                    onClick = { onTabSelected(tab) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.wrapContentWidth()
                ) { Text(tab) }
            }
        }
    }
}
