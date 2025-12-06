package com.example.penjualan_produk_umkm.owner.dashboard.produkScreen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
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
import androidx.room.Update
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProdukScreen(
    produk: Produk,
    onCancel: () -> Unit,
    navController: NavHostController,
    produkViewModel: ProdukViewModel
) {
    var nama by remember { mutableStateOf(produk.nama) }
    var deskripsi by remember { mutableStateOf(produk.deskripsi) }

    var spesifikasi by remember {
        mutableStateOf(
            if (produk.spesifikasi.isBlank()) {
                listOf("" to "")
            } else {
                produk.spesifikasi.split(",").map { item ->
                    val parts = item.split(":")
                    (parts.getOrNull(0) ?: "") to (parts.getOrNull(1) ?: "")
                }
            }
        )
    }

    var harga by remember { mutableStateOf(String.format(Locale("in", "ID"), "%,.0f", produk.harga)) }
    var hargaNumber by remember { mutableDoubleStateOf(produk.harga) }

    var stok by remember { mutableStateOf(produk.stok.toString()) }
    var kategori by remember { mutableStateOf(produk.kategori) }
    var currentGambarUrl by remember { mutableStateOf(produk.gambarUrl) }
    var newGambarUri by remember { mutableStateOf<Uri?>(null) }

    var showDialogBerhasil by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if(uri != null){
                newGambarUri = uri
                currentGambarUrl = "" // Kosongkan URL lama karena mau diganti baru
            }
        }
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val kategoriOptions = listOf("Aksesoris", "Spare Parts", "Sepeda", "Perawatan")
    var expandedKategori by remember { mutableStateOf(false) }

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
            snackbarHost = { SnackbarHost(snackbarHostState) },

            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray.copy(alpha = 0.1f),
                                contentColor = Color.DarkGray
                            ),
                            border = BorderStroke(2.dp, Color.Gray)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                val filteredSpecs = spesifikasi.filter { it.first.isNotBlank() || it.second.isNotBlank() }
                                val isAllSpecsEmpty = filteredSpecs.isEmpty()
                                val isAnyLabelEmpty = filteredSpecs.any { it.first.isBlank() }
                                val isAnyValue = filteredSpecs.any {it.second.isBlank() }

                                // Validasi
                                if (nama.isNotBlank() &&
                                    deskripsi.isNotBlank() &&
                                    harga.isNotBlank() &&
                                    stok.isNotBlank() &&
                                    kategori.isNotBlank()
                                ) {
                                    // Jika ada value tetapi label kosong → error
                                    if (isAnyLabelEmpty || isAnyValue) {
                                        scope.launch { snackbarHostState.showSnackbar("properti/value spesifikasi tidak boleh kosong") }
                                        return@Button
                                    }

                                    isLoading = true

                                    // Format spesifikasi
                                    val spesifikasiString =
                                        if (isAllSpecsEmpty) {
                                            "Tidak ada spesifikasi"
                                        } else {
                                            filteredSpecs.joinToString(",") { pair ->
                                                if (pair.second.isNotBlank()) "${pair.first}:${pair.second}"
                                                else pair.first
                                            }
                                        }

                                    val updatedProduk = produk.copy(
                                        nama = nama,
                                        deskripsi = deskripsi,
                                        spesifikasi = spesifikasiString,
                                        harga = hargaNumber,
                                        stok = stok.toIntOrNull() ?: 0,
                                        kategori = kategori
                                    )

                                    produkViewModel.updateProduk(
                                        produk = updatedProduk,
                                        newGambarUri = newGambarUri,
                                        context = context
                                    ) { success, errorMsg ->
                                        isLoading = false
                                        if (success) {
                                            showDialogBerhasil = true
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(errorMsg ?: "Gagal mengupdate produk")
                                            }
                                        }
                                    }
                                } else {
                                    val errorMessage = when {
                                        nama.isBlank() -> "Nama produk harus diisi"
                                        deskripsi.isBlank() -> "Deskripsi produk harus diisi"
                                        harga.isBlank() -> "Harga harus diisi"
                                        stok.isBlank() -> "Stok harus diisi"
                                        kategori.isBlank() -> "Kategori harus diisi"
                                        else -> "Semua field harus diisi"
                                    }
                                    scope.launch { snackbarHostState.showSnackbar(errorMessage) }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isLoading
                        ) {
                            Text("Simpan", color = Color.White)
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally)
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                        .clickable {
                            launcher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Logika menampilkan gambar (Prioritas: URI Baru -> URL Lama -> Placeholder)
                    if (newGambarUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(newGambarUri),
                            contentDescription = "Gambar Produk Baru",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (currentGambarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = currentGambarUrl,
                            contentDescription = "Gambar Produk Lama",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Klik untuk ganti foto",
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nama produk",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    // Inputan Nama Produk
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        placeholder = { Text("Contoh: Sepeda Gunung") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Deskripsi",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    // Inputan Deskripsi Produk
                    OutlinedTextField(
                        value = deskripsi,
                        onValueChange = { deskripsi = it },
                        placeholder = { Text("Deskripsikan produk Anda") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = Int.MAX_VALUE,
                        minLines = 3,
                    )
                }

                Column{
                    Text("Spesifikasi", style = MaterialTheme.typography.titleMedium)
                    spesifikasi.forEachIndexed { index, pair ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = pair.first,
                                onValueChange = {
                                    val newList = spesifikasi.toMutableList()
                                    newList[index] = pair.copy(first = it)
                                    spesifikasi = newList
                                },
                                label = { Text("Properti") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = pair.second,
                                onValueChange = {
                                    val newList = spesifikasi.toMutableList()
                                    newList[index] = pair.copy(second = it)
                                    spesifikasi = newList
                                },
                                label = { Text("Value") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(onClick = {
                                if (spesifikasi.size > 1) {
                                    val newList = spesifikasi.toMutableList()
                                    newList.removeAt(index)
                                    spesifikasi = newList
                                }
                            }) {
                                Icon(Icons.Default.Remove, contentDescription = "Hapus Spesifikasi")
                            }
                        }
                    }

                    Button(
                        onClick = { spesifikasi = spesifikasi + Pair("", "") },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Tambah Spesifikasi")
                    }
                }

                // Row inputan Harga & Stok
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Harga",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        // Inputan Harga
                        OutlinedTextField(
                            value = harga,
                            onValueChange = { newValue ->
                                val digits = newValue.filter { it.isDigit() }
                                val number = digits.toDoubleOrNull() ?: 0.0

                                hargaNumber = number

                                harga = if (digits.isNotEmpty()) {
                                    String.format(Locale("in", "ID"), "%,.0f", number)
                                } else {
                                    ""
                                }
                            },
                            prefix = { Text("Rp ") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Stok",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        // Inputan Stok
                        OutlinedTextField(
                            value = stok,
                            onValueChange = { stok = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("50") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    // DropDown Kategori
                    ExposedDropdownMenuBox(
                        expanded = expandedKategori,
                        onExpandedChange = { expandedKategori = !expandedKategori },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(
                                    topStart = 8.dp,
                                    topEnd = 8.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 0.dp
                                )
                            )

                    ) {
                        OutlinedTextField(
                            value = kategori,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Pilih kategori") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKategori)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryEditable,
                                    enabled = true
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedKategori,
                            onDismissRequest = { expandedKategori = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 0.dp
                                    )
                                )
                        ) {
                            kategoriOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            color = if (option == kategori) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    },
                                    onClick = {
                                        kategori = option
                                        expandedKategori = false
                                    },
                                    modifier = Modifier.background(
                                        if (option == kategori) MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.1f
                                        )
                                        else MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (showDialogBerhasil) {
                ProdukBerhasilDiEditDialog(onDismiss = {
                    showDialogBerhasil = false
                    navController.popBackStack()
                })
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
                text = "Barang telah berhasil diedit.",
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