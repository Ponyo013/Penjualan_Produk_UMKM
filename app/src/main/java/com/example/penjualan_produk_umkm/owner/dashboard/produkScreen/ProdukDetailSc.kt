package com.example.penjualan_produk_umkm.owner.dashboard.produkScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import java.util.Locale
import kotlin.text.ifEmpty

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdukDetailSc(
    produk: Produk?,
    navController: NavHostController,
    produkViewModel: ProdukViewModel,
) {
    var showDialog by remember { mutableStateOf(false) }

    UMKMTheme {
        Scaffold(
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("ulasan/${produk?.id}") },
                            modifier = Modifier.weight(0.3f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Button(
                            onClick = { navController.navigate("edit_produk/${produk?.id}") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Edit")
                        }

                        // Delete Button with red border
                        Button(
                            onClick = {
                                showDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Text(
                                text = "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        // Dialog konfirmasi
                        if (showDialog && produk != null) {
                            DeleteWarningDialog(
                                showDialog = showDialog,
                                onDismiss = { showDialog = false },
                                onConfirm = {
                                    produkViewModel.deleteProduk(produk)
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            // Image Box
            ImageBox(produk, navController)
        }
    }
}

@Composable
fun ProdukInfo(produk: Produk?) {
    val scrollState = rememberScrollState()

    if (produk == null) {
        Text(
            text = "Produk tidak ditemukan",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(scrollState),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nama Produk
                Text(
                    text = produk.nama,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Harga Produk
                Text(
                    text = "Rp ${String.format(Locale("in", "ID"), "%,.0f", produk.harga)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stok Produk
                    Text(
                        text = "${produk.stok} stok",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    // Barang Terjual
                    Text(
                        text = "${produk.terjual} terjual",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    
                    // Rating Produk
                    Text(
                        text = "${produk.rating} rating",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    thickness = 2.dp,
                )

                // Deskripsi
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Deskripsi",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = produk.deskripsi,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    thickness = 2.dp,
                )

                // Spesifikasi
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Spesifikasi",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = produk.spesifikasi,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Composable
fun ImageBox(produk: Produk?, navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Full width image
            if (produk != null) {
                val imageModel = produk.gambarUrl.ifEmpty { R.drawable.ic_error_image }
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Gambar Produk",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Row with status bar padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.statusBars.asPaddingValues()) // move padding to Row
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            }
        }
        ProdukInfo(produk)
    }
}

@Composable
fun DeleteWarningDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFFF6F00), // oranye
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Produk?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFD32F2F) // merah tua
                )
            },
            text = {
                Text(
                    text = "Tindakan ini tidak dapat dibatalkan. Yakin ingin menghapus produk ini?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F) // merah
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("Batal", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}