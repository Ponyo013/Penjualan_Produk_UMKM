package com.example.penjualan_produk_umkm.owner.dashboard

import androidx.compose.foundation.BorderStroke
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.penjualan_produk_umkm.model.Produk
import com.example.penjualan_produk_umkm.produkDummyList
import com.example.penjualan_produk_umkm.style.UMKMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdukManage(navController: NavHostController) {
    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Kelola Produk") }, navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                })
            },

            // Tombol tetap di bawah
            bottomBar = {
                Button(
                    onClick = {
                        navController.navigate("addProduk")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Tambah Produk Baru")
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search
                    SearchBar()

                    // Produk List
                    ProdukList(
                        produkItems = produkDummyList,
                        onEditClick = { produk -> },
                        onHapusClick = { produk -> }
                    )
                }
            }

        }
    }
}

@Composable
fun SearchBar() {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = {
            Text(
                text = "Cari Produk...",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
            )
        },
        textStyle = LocalTextStyle.current.copy(
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.tertiary
        ),
    )
}


@Composable
fun ProdukList(
    produkItems: List<Produk>, onEditClick: (Produk) -> Unit, onHapusClick: (Produk) -> Unit
) {
    if (produkItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada produk",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
            ,
        ) {
            produkItems.forEach { produk ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Gambar di kiri
                        AsyncImage(
                            model = "https://via.placeholder.com/150",
                            contentDescription = "Contoh Gambar",
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Crop
                        )

                        // Informasi barang
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = produk.nama, style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Rp ${produk.harga}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.wrapContentWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(18.dp),
                                            imageVector = Icons.Outlined.Inbox,
                                            contentDescription = "Stock",
                                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "Stok: ${produk.stok}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                                fontWeight = FontWeight.Normal
                                            )
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(18.dp),
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = "Terjual",
                                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "Terjual: ${produk.terjual}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                                fontWeight = FontWeight.Normal
                                            )
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.wrapContentWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(18.dp),
                                            imageVector = Icons.Outlined.StarRate,
                                            contentDescription = "Rating",
                                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "Rating: ${produk.rating}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                                fontWeight = FontWeight.Normal
                                            )
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(18.dp),
                                            imageVector = Icons.Outlined.RemoveRedEye,
                                            contentDescription = "Dilihat",
                                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "Dilihat: 0",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                                fontWeight = FontWeight.Normal
                                            )
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.wrapContentWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Edit
                                Button(
                                    onClick = { onEditClick(produk) }, modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "Edit")
                                }

                                // Hapus
                                OutlinedButton(
                                    onClick = { onHapusClick(produk) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent, contentColor = Color.Red
                                    ),
                                    border = BorderStroke(1.dp, Color.Red),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "Hapus", color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    val fakeNavController = rememberNavController()
    ProdukManage(fakeNavController)
}
