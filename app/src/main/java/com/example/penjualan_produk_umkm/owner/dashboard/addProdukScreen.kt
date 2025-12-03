package com.example.penjualan_produk_umkm.owner.dashboard

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProdukScreen(
    navController: NavHostController, produkViewModel: ProdukViewModel
) {
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var spesifikasi by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var stok by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }
    var expandedKategori by remember { mutableStateOf(false) }
    val kategoriOptions = listOf("Aksesoris", "Spare Parts", "Sepeda")
    var gambarUri by remember { mutableStateOf<Uri?>(null) }

    var showDialogBerhasil by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> gambarUri = uri }
    )

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    UMKMTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                isLoading = true

                                // Simpan produk, handling upload di ViewModel
                                saveProduct(
                                    nama = nama,
                                    deskripsi = deskripsi,
                                    spesifikasi = spesifikasi,
                                    harga = harga,
                                    stok = stok,
                                    kategori = kategori,
                                    gambarUri = gambarUri, // Bisa null
                                    viewModel = produkViewModel,
                                    context = context,
                                    onSuccess = { showDialogBerhasil = true },
                                    onComplete = { isLoading = false }
                                )
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Semua field harus diisi") }
                            }
                        },
                        enabled = !isLoading, // Disable tombol saat loading
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Tambahkan Produk")
                        }
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
                            .clickable {
                                pickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ,
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

                if (showDialogBerhasil) {
                    ProdukBerhasilDialog(
                        onDismiss = {
                            showDialogBerhasil = false
                            navController.popBackStack() // Kembali ke halaman sebelumnya setelah sukses
                        }
                    )
                }
            }
        }
    }
}

// Fungsi Helper untuk menyimpan data ke Firestore
fun saveProduct(
    nama: String,
    deskripsi: String,
    spesifikasi: String,
    harga: String,
    stok: String,
    kategori: String,
    gambarUri: Uri?, // Bisa kosong kalau sudah ada imageKitResult
    viewModel: ProdukViewModel,
    context: Context,
    onSuccess: () -> Unit,
    onComplete: () -> Unit
) {
    val produk = Produk(
        id = "",
        nama = nama,
        deskripsi = deskripsi,
        spesifikasi = spesifikasi,
        harga = harga.toDoubleOrNull() ?: 0.0,
        stok = stok.toIntOrNull() ?: 0,
        kategori = kategori,
        gambarUrl = "",
        imageKitFileId = "",
        rating = 0f,
        terjual = 0
    )

    viewModel.insertProduk(produk, gambarUri, context) { success, errorMsg ->
        if (success) onSuccess()
        else Log.e("SAVE_PRODUCT", errorMsg ?: "Unknown error")
        onComplete()
    }
}



// Fungsi Helper untuk Upload Gambar ke Firebase Storage


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