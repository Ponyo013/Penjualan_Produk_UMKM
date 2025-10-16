package com.example.penjualan_produk_umkm.uiComponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.penjualan_produk_umkm.style.Secondary2
import com.example.penjualan_produk_umkm.style.Secondary3
import  com.example.penjualan_produk_umkm.style.Montserrat

@Preview
@Composable
fun SearchBarPreview() {
    // You can pass a dummy lambda since it's just a preview
    SearchBar(
        onSearch = { query -> /* no-op */ })
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier, onSearch: (String) -> Unit
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }

    OutlinedTextField(
        value = textState,
        onValueChange = { newValue ->
            textState = newValue
            onSearch(newValue.text)
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
        textStyle = LocalTextStyle.current.copy(
            fontSize = 16.sp,
            fontFamily = Montserrat,
            color = Secondary3
        ),
        trailingIcon = {
            if (textState.text.isNotEmpty()) {
                IconButton(onClick = {
                    textState = TextFieldValue("") //
                    onSearch("")
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Text",
                        tint = Secondary2
                    )
                }
            }
        },
    )

}
