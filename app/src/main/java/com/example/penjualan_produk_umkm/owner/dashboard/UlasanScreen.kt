package com.example.penjualan_produk_umkm.owner.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.UlasanViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UlasanScreen(
    produkId: String,
    navController: NavController,
    ulasanViewModel: UlasanViewModel
) {
    // FIX 1: Panggil fungsi load data di dalam LaunchedEffect
    LaunchedEffect(produkId) {
        ulasanViewModel.getUlasanByProdukId(produkId)
    }

    // FIX 2: Observasi LiveData secara terpisah
    val ulasanList by ulasanViewModel.ulasanList.observeAsState(emptyList())

    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Ulasan Produk") }, navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                })
            },
        ) { paddingValues ->
            if (ulasanList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada ulasan untuk produk ini")
                }
            } else {
                LazyColumn(
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(ulasanList) { ulasan ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(ulasan.rating.toInt()) {
                                        Icon(
                                            imageVector = Icons.Outlined.Star,
                                            contentDescription = "Rating",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Tampilkan nama user jika ada, atau ID sebagian
                                    val displayName = ulasan.userName ?: "User ${ulasan.userId.take(4)}..."

                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ulasan.komentar,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // FIX 3: Format Tanggal Firebase Timestamp
                                val dateString = try {
                                    val date = ulasan.tanggal.toDate()
                                    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                                    formatter.format(date)
                                } catch (e: Exception) {
                                    "-"
                                }

                                Text(
                                    text = dateString,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray // Ganti MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) agar lebih simpel atau sesuaikan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}