// File: com/example/penjualan_produk_umkm/uiComponent/ProductFilterControls.kt

package com.example.penjualan_produk_umkm.uiComponent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.style.Secondary2

// Enum untuk opsi sorting
enum class ProductSortOption(val label: String) {
    TERBARU("Terbaru"),
    TERLARIS("Terlaris"),
    HARGA_MAHAL("Harga Termahal"),
    HARGA_MURAH("Harga Termurah")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFilterControls(
    totalItemsCount: Int,
    currentSort: ProductSortOption,
    onSortChange: (ProductSortOption) -> Unit,
    onStockFilterChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var readyStockChecked by remember { mutableStateOf(false) }

    // Dropdown options
    val sortOptions = ProductSortOption.entries

    Column(modifier = Modifier.fillMaxWidth()) {
        // Teks "Menampilkan X dari Y produk"
        Text(
            text = "Menampilkan 12 dari $totalItemsCount produk",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Dropdown Sort (Left Side)
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = currentSort.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Urutkan") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary2,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                onSortChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 2. Checkbox "Hanya ready stock" (Right Side)
            Row(
                modifier = Modifier
                    .weight(1f)
                    // HAPUS .fillMaxHeight() di sini:
                    // .fillMaxHeight() // 👈 HAPUS BARIS INI
                    .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp))
                    .clickable {
                        readyStockChecked = !readyStockChecked
                        onStockFilterChange(readyStockChecked)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp), // 👈 Tambahkan padding vertikal
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = readyStockChecked,
                    onCheckedChange = { isChecked ->
                        readyStockChecked = isChecked
                        onStockFilterChange(isChecked)
                    },
                    colors = CheckboxDefaults.colors(checkedColor = Secondary2)
                )
                Text(
                    text = "Hanya ready stock",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductFilterControlsPreview() {
    UMKMTheme {
        ProductFilterControls(
            totalItemsCount = 184,
            currentSort = ProductSortOption.TERBARU,
            onSortChange = {},
            onStockFilterChange = {}
        )
    }
}