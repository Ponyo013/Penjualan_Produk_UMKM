package com.example.penjualan_produk_umkm.uiComponent

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.penjualan_produk_umkm.style.Montserrat
import com.example.penjualan_produk_umkm.style.Secondary2
import com.example.penjualan_produk_umkm.style.Secondary3

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit, // Callback saat mengetik (untuk filter realtime)
    onSearchClicked: (String) -> Unit // Callback saat tombol Search ditekan (untuk save history)
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = textState,
        onValueChange = { newValue ->
            textState = newValue
            onQueryChange(newValue.text) // Panggil setiap ketik
        },
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Cari Produk...",
                color = Secondary3,
                fontSize = 14.sp,
                fontFamily = Montserrat,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Secondary2
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Secondary2,
            unfocusedIndicatorColor = Secondary2
        ),
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontFamily = Montserrat,
            color = Secondary3
        ),
        trailingIcon = {
            if (textState.text.isNotEmpty()) {
                IconButton(onClick = {
                    textState = TextFieldValue("")
                    onQueryChange("") // Clear filter
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Text",
                        tint = Secondary2
                    )
                }
            }
        },
        // --- TAMBAHKAN INI UNTUK TOMBOL KEYBOARD ---
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchClicked(textState.text) // Panggil hanya saat enter
                keyboardController?.hide() // Tutup keyboard
            }
        )
    )
}