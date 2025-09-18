package com.example.penjualan_produk_umkm.client.ui.beranda

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable

@Preview
@Composable
fun SearchBarPreview() {
    // You can pass a dummy lambda since it's just a preview
    SearchBar(
        onSearch = { query -> /* no-op */ }
    )
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }

    TextField(
        value = textState,
        onValueChange = { newValue ->
            textState = newValue
            onSearch(newValue.text)
        },
        modifier = modifier
            .fillMaxWidth(),
        placeholder = { Text("Cari Produk...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    )

}
