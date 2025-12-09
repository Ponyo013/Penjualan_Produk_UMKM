package com.example.penjualan_produk_umkm.owner.dashboard.produkScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdukManage(
    navController: NavHostController,
    produkViewModel: ProdukViewModel
) {
    val produkList by produkViewModel.allProduk.observeAsState(initial = emptyList())


    UMKMTheme {

        var searchText by remember { mutableStateOf("") }
        var showFilter by remember { mutableStateOf(false) }
        var selectedFilters by remember { mutableStateOf(setOf<String>()) }
        var pendingFilters by remember { mutableStateOf(setOf<String>()) }

        val filteredProduk = produkList
            .filter { it.nama.contains(searchText, ignoreCase = true) }
            .let { list ->
                var result = list

                if ("stok_terendah" in selectedFilters) result = result.sortedBy { it.stok }
                if ("stok_tertinggi" in selectedFilters) result = result.sortedByDescending { it.stok }

                if ("terjual_terendah" in selectedFilters) result = result.sortedBy { it.terjual }
                if ("terjual_tertinggi" in selectedFilters) result = result.sortedByDescending { it.terjual }

                if ("rating_terendah" in selectedFilters) result = result.sortedBy { it.rating }
                if ("rating_tertinggi" in selectedFilters) result = result.sortedByDescending { it.rating }

                result
            }


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
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Button(
                        onClick = {
                            navController.navigate("addProduk")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Tambah Produk Baru")
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SearchBar(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier
                                .weight(1f).height(48.dp)
                        )

                        Button(
                            onClick = { showFilter = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                            )
                        }
                    }

                    // Produk List
                    ProdukList(
                        produkItems = filteredProduk,
                        onProdukClick = { produk ->
                            navController.navigate("detail/${produk.id}")
                        }
                    )
                }
            }
        }

        if (showFilter) {
            ModalBottomSheet(
                onDismissRequest = { showFilter = false }
            ) {
                FilterSheetContent(
                    pendingFilters = pendingFilters,
                    onToggle = { filterKey ->
                        pendingFilters = if (pendingFilters.contains(filterKey)) {
                            pendingFilters - filterKey
                        } else {
                            pendingFilters + filterKey
                        }
                    },
                    onApply = {
                        selectedFilters = pendingFilters
                        showFilter = false
                    },
                    onClose = { showFilter = false }
                )
            }
        }

    }
}

@Composable
fun FilterSheetContent(
    pendingFilters: Set<String>,
    onToggle: (String) -> Unit,
    onApply: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Filter Produk", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Stok", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableFilterButton(
                    text = "Terendah",
                    selected = "stok_terendah" in pendingFilters,
                    onClick = { onToggle("stok_terendah") }
                )

                SelectableFilterButton(
                    text = "Tertinggi",
                    selected = "stok_tertinggi" in pendingFilters,
                    onClick = { onToggle("stok_tertinggi") }
                )

            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Terjual", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                SelectableFilterButton(
                    text = "Terendah",
                    selected = "terjual_terendah" in pendingFilters,
                    onClick = { onToggle("terjual_terendah") }
                )

                SelectableFilterButton(
                    text = "Tertinggi",
                    selected = "terjual_tertinggi" in pendingFilters,
                    onClick = { onToggle("terjual_tertinggi") }
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text("Rating", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableFilterButton(
                    text = "Terendah",
                    selected = "rating_terendah" in pendingFilters,
                    onClick = { onToggle("rating_terendah") }
                )
                SelectableFilterButton(
                    text = "Tertinggi",
                    selected = "rating_tertinggi" in pendingFilters,
                    onClick = { onToggle("rating_tertinggi") }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // APPLY BUTTON
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Apply Filter")
            }

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Close")
            }

        }

    }
}

@Composable
fun SelectableFilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {


    if (selected) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text)
        }

    } else {
        // Unselected → Putih + border biru
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text(text)
        }
    }
}


@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),

        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (value.isEmpty()) {
                        Text(
                            "Cari produk...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    innerTextField()
                }
            }
        }
    )

}


@Composable
fun ProdukList(
    produkItems: List<Produk>,
    onProdukClick: (Produk) -> Unit
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
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            produkItems.forEach { produk ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    onClick = { onProdukClick(produk) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Gambar Barang
                        val imageModel = produk.gambarUrl.ifEmpty { R.drawable.ic_error_image }

                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Gambar Produk",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Informasi Barang
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Nama Produk
                            Text(
                                text = produk.nama,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            // Harga Produk
                            Text(
                                text = "Rp ${
                                    String.format(
                                        Locale("in", "ID"),
                                        "%,.0f",
                                        produk.harga
                                    )
                                }",
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${produk.rating} rating",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        thickness = 2.dp,
                    )
                }
            }
        }
    }
}