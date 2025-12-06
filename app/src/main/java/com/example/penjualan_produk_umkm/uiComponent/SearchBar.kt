package com.example.penjualan_produk_umkm.uiComponent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.penjualan_produk_umkm.style.Montserrat
import com.example.penjualan_produk_umkm.style.Secondary2
import com.example.penjualan_produk_umkm.style.Secondary3

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    readOnly: Boolean = false, // Mode Beranda (Hanya Tombol)
    autoFocus: Boolean = false, // Mode Search Page (Langsung Keyboard)
    onClick: (() -> Unit)? = null, // Aksi klik saat readOnly
    onQueryChange: (String) -> Unit = {},
    onSearchClicked: (String) -> Unit = {}
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Logic Auto Focus (Untuk Search Page)
    LaunchedEffect(Unit) {
        if (autoFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = textState,
            onValueChange = { newValue ->
                textState = newValue
                onQueryChange(newValue.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester), // Pasang Focus Requester
            readOnly = readOnly, // Jika True, keyboard tidak akan muncul saat diklik
            placeholder = {
                Text(
                    text = "Cari Produk",
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
                if (textState.text.isNotEmpty() && !readOnly) {
                    IconButton(onClick = {
                        textState = TextFieldValue("")
                        onQueryChange("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Text",
                            tint = Secondary2
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchClicked(textState.text)
                    keyboardController?.hide()
                }
            )
        )

        // TRICK: Overlay transparan untuk menangkap klik di Beranda
        if (readOnly) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onClick?.invoke() }
            )
        }
    }
}