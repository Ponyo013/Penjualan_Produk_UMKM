package com.example.penjualan_produk_umkm.owner.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.penjualan_produk_umkm.model.Produk
import com.example.penjualan_produk_umkm.produkDummyList
import com.example.penjualan_produk_umkm.style.UMKMTheme

// TAMBAHKAN BARIS INI
import com.example.penjualan_produk_umkm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProdukScreen(navController: NavHostController) {
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var spesifikasi by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var stok by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }
    var expandedKategori by remember { mutableStateOf(false) }
    val kategoriOptions = listOf("Sparepart", "MTB", "BMX", "Mini", "Sepeda Anak")

    var showDialogBerhasil by remember { mutableStateOf(false) }

    var gambarUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            gambarUri = uri
        }
    }

    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Tambah Produk Baru") }, navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                })
            },

            // 🔽 Tambahkan bottom bar di sini
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Button(
                        onClick = {
                            if (nama.isNotBlank() && deskripsi.isNotBlank() && spesifikasi.isNotBlank()
                                && harga.isNotBlank() && stok.isNotBlank() && kategori.isNotBlank()
                            ) {
                                val produk = Produk(
                                    id = produkDummyList.maxOfOrNull { it.id }?.plus(1) ?: 1,
                                    nama = nama,
                                    deskripsi = deskripsi,
                                    spesifikasi = spesifikasi,
                                    harga = harga.toDouble(),
                                    stok = stok.toInt(),
                                    kategori = kategori,
                                    gambarResourceIds = listOf(R.drawable.ic_empty_star),
                                    rating = 0f,
                                    terjual = 0
                                )
                                produkDummyList.add(produk)
                                showDialogBerhasil = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tambahkan Produk")
                    }
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
                    modifier = Modifier
                        .fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),

                    ) {
                    // Card Pilih Gambar
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.CenterHorizontally)
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (gambarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(gambarUri),
                                contentDescription = "Gambar Produk",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Text(text = "Klik untuk memilih gambar",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth() )
                        }
                    }

                    // Inputan Nama Produk
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama Produk") },
                        placeholder = { Text("Contoh: Sepeda Gunung") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )

                    // Inputan Deskripsi Produk
                    OutlinedTextField(
                        value = deskripsi,
                        onValueChange = { deskripsi = it },
                        label = { Text("Deskripsi Produk") },
                        placeholder = { Text("Deskripsikan produk Anda") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = Int.MAX_VALUE,
                        minLines = 3,
                    )

                    OutlinedTextField(
                        value = spesifikasi,
                        onValueChange = { spesifikasi = it },
                        label = { Text("Spesifikasi Produk") },
                        placeholder = { Text("Contoh: Rangka Carbon, Suspensi 100mm") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = Int.MAX_VALUE,
                        minLines = 3,
                    )

                    // Row inputan Harga & Stok
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = harga,
                            onValueChange = { harga = it.filter { c -> c.isDigit() } },
                            label = { Text("Harga") },
                            placeholder = { Text("100000") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        )
                        OutlinedTextField(
                            value = stok,
                            onValueChange = { stok = it.filter { c -> c.isDigit() } },
                            label = { Text("Stok") },
                            placeholder = { Text("50") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                    // DropDown Kategori
                    ExposedDropdownMenuBox(
                        expanded = expandedKategori,
                        onExpandedChange = { expandedKategori = !expandedKategori },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = kategori,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKategori) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = expandedKategori,
                            onDismissRequest = { expandedKategori = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            kategoriOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        kategori = option
                                        expandedKategori = false
                                    },
                                )
                            }
                        }
                    }
                }

                // Panggil dialog jika diperlukan
                if (showDialogBerhasil) {
                    ProdukBerhasilDialog(
                        onDismiss = { showDialogBerhasil = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ProdukBerhasilDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF4CAF50), // hijau
                modifier = Modifier.size(64.dp)
            )
        },
        title = {
            Text(
                text = "Berhasil!",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF388E3C), // hijau tua
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Barang telah berhasil ditambahkan ke daftar produk.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // hijau utama
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("OK", color = Color.White)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFFF1F8E9)
    )
}

@Composable
@Preview
fun AddProdukScreenPreview() {
    val fakeNavController = rememberNavController()
    AddProdukScreen(fakeNavController)
}
