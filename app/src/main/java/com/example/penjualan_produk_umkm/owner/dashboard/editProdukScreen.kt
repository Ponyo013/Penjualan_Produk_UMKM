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
import com.example.penjualan_produk_umkm.style.UMKMTheme
import kotlinx.coroutines.launch
import com.example.penjualan_produk_umkm.R
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProdukScreen(
    produk: Produk,
    onSave: (Produk) -> Unit,
    onCancel: () -> Unit,
    navController: NavHostController
) {
    var nama by remember { mutableStateOf(produk.nama) }
    var deskripsi by remember { mutableStateOf(produk.deskripsi) }
    var spesifikasi by remember { mutableStateOf(produk.spesifikasi) }
    var harga by remember { mutableStateOf(produk.harga.toString()) }
    var stok by remember { mutableStateOf(produk.stok.toString()) }
    var kategori by remember { mutableStateOf(produk.kategori) }

    var showDialogBerhasil by remember { mutableStateOf(false) }

    var gambarResourceId by remember { mutableStateOf<Int?>(produk.gambarResourceIds.firstOrNull()) } // <-- BARU: Ambil ID Int pertama
    var newGambarUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            newGambarUri = uri
            gambarResourceId = null // Hapus resource lama saat URI baru dipilih
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Produk") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali"
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ====== Upload / Ganti Gambar ======
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.CenterHorizontally)
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (newGambarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(newGambarUri),
                                contentDescription = "Gambar Produk",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (gambarResourceId != null && gambarResourceId != 0) {
                            Image(
                                painter = rememberAsyncImagePainter(gambarResourceId), // Tampilkan ID resource (Int)
                                contentDescription = "Gambar Produk",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "Klik untuk ganti foto", // <-- Pastikan ini string yang benar
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    // ====== Input Fields ======
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama Produk") },
                        placeholder = { Text("Contoh: Sepeda Gunung") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = deskripsi,
                        onValueChange = { deskripsi = it },
                        label = { Text("Deskripsi Produk") },
                        placeholder = { Text("Deskripsikan produk Anda") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = spesifikasi,
                        onValueChange = { spesifikasi = it },
                        label = { Text("Spesifikasi Produk") },
                        placeholder = { Text("Spesifikasikan produk Anda") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = harga,
                            onValueChange = { harga = it.filter { c -> c.isDigit() } },
                            label = { Text("Harga") },
                            placeholder = { Text("100000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = stok,
                            onValueChange = { stok = it.filter { c -> c.isDigit() } },
                            label = { Text("Stok") },
                            placeholder = { Text("50") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Pilihan kategori
                    val kategoriOptions = listOf("MTB", "BMX", "Sepeda Anak", "Road Bike", "Lainnya")
                    var expandedKategori by remember { mutableStateOf(false) }

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
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = expandedKategori,
                            onDismissRequest = { expandedKategori = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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

                    // ====== Action Buttons ======
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                onCancel()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Batal")
                        }


                        Button(
                            onClick = {
                                if (nama.isBlank() || deskripsi.isBlank() || spesifikasi.isBlank() || harga.isBlank() || // <-- VALIDASI DIPERBARUI
                                    stok.isBlank() || kategori.isBlank()
                                ) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Semua field wajib diisi!")
                                    }
                                } else {
                                    val currentImageIdList: List<Int> = if (newGambarUri != null) {
                                        // Jika ada URI baru, simpan placeholder (simulasi upload berhasil)
                                        // Asumsi R.drawable.ic_empty_star adalah placeholder Int
                                        listOf(R.drawable.ic_empty_star)
                                    } else if (gambarResourceId != null) {
                                        // Jika tidak ada URI baru, gunakan kembali ID Int lama
                                        listOf(gambarResourceId!!)
                                    } else {
                                        // Tidak ada gambar
                                        emptyList()
                                    }
                                    val updatedProduk = produk.copy(
                                        nama = nama,
                                        deskripsi = deskripsi,
                                        spesifikasi = spesifikasi,
                                        harga = harga.toDoubleOrNull() ?: produk.harga,
                                        stok = stok.toIntOrNull() ?: produk.stok,
                                        kategori = kategori,
                                        // BARIS 257, 346, 348: PARAMETER HARUS COCOK DENGAN PRODUK.KT
                                        gambarResourceIds = currentImageIdList // <-- PARAMETER BARU YANG BENAR
                                    )
                                    onSave(updatedProduk)
                                    showDialogBerhasil = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Simpan", color = Color.White)
                        }
                    }
                }
                // Panggil dialog jika diperlukan
                if (showDialogBerhasil) {
                    ProdukBerhasilDiEditDialog(
                        onDismiss = { showDialogBerhasil = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ProdukBerhasilDiEditDialog(onDismiss: () -> Unit) {
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
                text = "Barang telah berhasil edit.",
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


@Preview(showBackground = true)
@Composable
fun PreviewEditProdukScreen() {
    val dummyProduk = Produk(
        id = 1,
        nama = "Sepeda Gunung",
        deskripsi = "Sepeda gunung dengan suspensi ganda dan frame ringan",
        // >>> PARAMETER 'SPESIFIKASI' YANG HILANG DITAMBAHKAN DI SINI <<<
        spesifikasi = "Rangka: Alloy, Rem: Hidrolik", // <-- TAMBAHKAN BARIS INI
        harga = 2500000.0,
        stok = 10,
        kategori = "Olahraga",
        gambarResourceIds = listOf(R.drawable.ic_empty_star),
        rating = 4.5f,
        terjual = 100
    )

    val fakeNavController = rememberNavController()
    EditProdukScreen(
        produk = dummyProduk,
        onSave = {},
        onCancel = {},
        navController = fakeNavController
    )
}