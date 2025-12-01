package com.example.penjualan_produk_umkm.owner.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.penjualan_produk_umkm.database.model.StatusPesanan
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.OwnerPesananViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Keuangan(navController: NavController, viewModel: OwnerPesananViewModel){

    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Laporan Keuangan") }, navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                })
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Laporan berdasarkan tanggal
                    LaporanDenganKalender(viewModel)
                }
            }
        }
    }
}

@Composable
fun LaporanDenganKalender(viewModel: OwnerPesananViewModel) {
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf<String?>(null) }
    val pesananList by viewModel.pesananList.collectAsState()
    val itemList by viewModel.getAllItems().observeAsState(emptyList())

    // Tombol Tab: Bulanan / Tahunan
    Row(
        modifier = Modifier
            .wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tombol Pilih Tanggal
        OutlinedButton(
            onClick = { showDatePicker = true },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Text(
                text = pickedDate.toString(),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        val tabs = listOf("Bulanan", "Tahunan")
        tabs.forEach { tab ->
            if (selectedTab == tab) {
                Button(
                    onClick = { selectedTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(tab)
                }
            } else {
                OutlinedButton(
                    onClick = {selectedTab = tab  },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(tab)
                }
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateChange = {
                pickedDate = it
                selectedTab = null
                showDatePicker = false
            }
        )
    }

    // Filter pesanan
    val filteredPesanan = when (selectedTab) {
        null -> pesananList.filter { it.pesanan.tanggal == pickedDate }
        "Bulanan" -> pesananList.filter { it.pesanan.tanggal.year == pickedDate.year } // ambil semua bulan di tahun yang sama
        "Tahunan" -> pesananList // tampilkan semua tahun
        else -> emptyList()
    }.filter {
        it.pesanan.status == StatusPesanan.DIKIRIM || it.pesanan.status == StatusPesanan.SELESAI
    }


    if (filteredPesanan.isEmpty()) {
        Text(
            text = when (selectedTab) {
                null -> "Tidak ada pesanan pada tanggal $pickedDate"
                "Bulanan" -> "Tidak ada pesanan pada bulan ${pickedDate.month}"
                "Tahunan" -> "Tidak ada pesanan pada tahun ${pickedDate.year}"
                else -> ""
            },
            modifier = Modifier.padding(16.dp)
        )
    } else {
        // Group data jika Bulanan atau Tahunan
        val groupedPesanan = when (selectedTab) {
            "Bulanan" -> filteredPesanan.groupBy { it.pesanan.tanggal.monthValue  } // group per bulan
            "Tahunan" -> filteredPesanan.groupBy { it.pesanan.tanggal.year } // group per tahun
            else -> mapOf(null to filteredPesanan)
        }

        LazyColumn(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedPesanan.forEach { (group, pesananList) ->
                item {
                    if (group != null) {
                        Text(
                            text = when (selectedTab) {

                                "Bulanan" -> {
                                    val month = Month.of(group)
                                    month.name.lowercase()
                                        .replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase(Locale("id")) else it.toString()
                                        }
                                }
                                "Tahunan" -> group.toString()
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        )
                    }
                }


                items(pesananList) { pesanan ->
                    LaporanPenjualanCard(pesanan, viewModel)
                }
            }
        }
    }
}

@Composable
fun LaporanPenjualanCard(pesanan: PesananWithItems, viewModel: OwnerPesananViewModel) {
    val itemsForPesanan by viewModel.getItemsForPesanan(pesanan.pesanan.id).observeAsState(initial = emptyList())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: ID
            Text(
                text = "Pesanan #${pesanan.pesanan.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(modifier = Modifier.height(8.dp))

            // Info user, tanggal & metode pembayaran
            Column(modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text( text = "Customer: ${pesanan.user?.nama ?: "Tidak diketahui"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Tanggal: ${pesanan.pesanan.tanggal}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Ekspedisi: ${pesanan.ekspedisi?.nama ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Pembayaran: ${pesanan.pesanan.metodePembayaran.name}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(modifier = Modifier.height(12.dp))

            // List item
            Column(modifier = Modifier.fillMaxWidth()) {
                itemsForPesanan.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.produk.nama} (x${item.itemPesanan.jumlah})",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Rp ${item.produk.harga * item.itemPesanan.jumlah}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(modifier = Modifier.height(8.dp))

            // Total harga & jumlah item
            val totalHarga = itemsForPesanan.sumOf { it.produk.harga * it.itemPesanan.jumlah }
            val totalItem = itemsForPesanan.sumOf { it.itemPesanan.jumlah }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Item: $totalItem",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total: Rp $totalHarga",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateChange: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today.toEpochDay() * 24 * 60 * 60 * 1000
    )
    Dialog(onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title
                Text(
                    "Pilih Tanggal",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                // DatePicker
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth(),
                    headline = {
                        Text(
                            datePickerState.selectedDateMillis?.let {
                                val selectedDate = LocalDate.ofEpochDay(it / (24*60*60*1000))
                                selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                            } ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        dayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                        todayContentColor = MaterialTheme.colorScheme.secondary,
                        todayDateBorderColor = MaterialTheme.colorScheme.secondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Batal", color = MaterialTheme.colorScheme.secondary)
                    }
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val selectedDate = LocalDate.ofEpochDay(it / (24*60*60*1000))
                            onDateChange(selectedDate)
                            onDismissRequest()
                        }
                    }) {
                        Text("OK", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}