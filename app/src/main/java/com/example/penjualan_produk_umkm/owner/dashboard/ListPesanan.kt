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

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            "Baru" -> viewModel.loadByStatus(StatusPesanan.DIPROSES)
            "Diproses" -> viewModel.loadByStatus(StatusPesanan.DIPROSES)
            "Dikirim" -> viewModel.loadByStatus(StatusPesanan.DIKIRIM)
            "Selesai" -> viewModel.loadByStatus(StatusPesanan.SELESAI)
            "Batal" -> viewModel.loadByStatus(StatusPesanan.DIBATALKAN)
            else -> viewModel.loadAll()
        }
    }

    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pesanan Masuk") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                TabPesanan(selectedTab) { selectedTab = it }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pesananList) { pesananLengkap ->
                        CardPesanan(
                            pesananLengkap = pesananLengkap,
                            onStatusChange = { newStatus ->
                                viewModel.updateStatus(pesananLengkap.pesanan.id, newStatus)
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            // List Item Produk (Ambil dari PesananLengkap, tidak perlu query lagi)
            pesananLengkap.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gunakan AsyncImage (Coil) untuk URL, atau Placeholder jika kosong
                    // ItemPesanan tidak simpan URL gambar, jadi kita pakai placeholder default
                    AsyncImage(
                        model = com.example.penjualan_produk_umkm.R.drawable.ic_error_image,
                        contentDescription = item.produkNama,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Text(text = item.produkNama, style = MaterialTheme.typography.bodyMedium)
                    Text(text = "x${item.jumlah}")
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
                text = "Ekspedisi: -", // Placeholder karena data ekspedisi tidak di-join di ViewModel saat ini
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
                    title = { Text("Edit Status Pesanan", style = MaterialTheme.typography.titleLarge) },
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
                                    StatusPesanan.values().forEach { status ->
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