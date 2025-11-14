package com.example.penjualan_produk_umkm.owner.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.EkspedisiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpedisiScreen(navController: NavHostController, viewModel: EkspedisiViewModel) {
    val ekspedisiList by viewModel.list.observeAsState(emptyList())

    // Load data saat pertama kali
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Daftar Ekspedisi") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Toggle di bawah untuk mengaktifkan atau mematikan ekspedisi pada semua produk toko Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ekspedisiList) { ekspedisi ->
                        var isChecked by remember(ekspedisi.id) { mutableStateOf(ekspedisi.isActive) }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = ekspedisi.nama,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    ekspedisi.layanan?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Switch(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        isChecked = checked
                                        ekspedisi.isActive = checked
                                        viewModel.update(ekspedisi)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
