package com.example.penjualan_produk_umkm.owner.dashboard.penjualan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.OwnerPesananViewModel
import com.example.penjualan_produk_umkm.viewModel.PesananLengkap
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Keuangan(navController: NavController, viewModel: OwnerPesananViewModel){

    // Load semua pesanan (realtime dari Firestore)
    val pesananList by viewModel.pesananList.collectAsState(initial = emptyList())

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
                    LaporanDenganKalender(pesananList)
                }
            }
        }
    }
}

@Composable
fun LaporanDenganKalender(pesananList: List<PesananLengkap>) {
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf<String?>(null) }

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
    // Kita perlu konversi tanggal Firestore (Timestamp) ke LocalDate untuk dibandingkan
    val filteredPesanan = pesananList.filter { pesananLengkap ->
        val status = pesananLengkap.pesanan.status
        // Filter Status: Hanya DIKIRIM atau SELESAI
        val isValidStatus = status == StatusPesanan.DIKIRIM.name || status == StatusPesanan.SELESAI.name

        if (!isValidStatus) return@filter false

        // Konversi Tanggal
        val date = try {
            val timestamp = pesananLengkap.pesanan.tanggal.toDate()
            val cal = Calendar.getInstance()
            cal.time = timestamp
            LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        } catch (e: Exception) {
            LocalDate.now() // Fallback
        }

        when (selectedTab) {
            null -> date == pickedDate
            "Bulanan" -> date.year == pickedDate.year && date.month == pickedDate.month
            "Tahunan" -> date.year == pickedDate.year
            else -> false
        }
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
        // Grouping Logic
        // Kita perlu ambil tanggal lagi untuk grouping
        val groupedPesanan = when (selectedTab) {
            "Bulanan" -> filteredPesanan.groupBy {
                // Group by Month Value
                val timestamp = it.pesanan.tanggal.toDate()
                val cal = Calendar.getInstance()
                cal.time = timestamp
                cal.get(Calendar.MONTH) + 1
            }
            "Tahunan" -> filteredPesanan.groupBy {
                // Group by Year
                val timestamp = it.pesanan.tanggal.toDate()
                val cal = Calendar.getInstance()
                cal.time = timestamp
                cal.get(Calendar.YEAR)
            }
            else -> mapOf(null to filteredPesanan)
        }

        LazyColumn(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedPesanan.forEach { (group, list) ->
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

                items(list) { pesananLengkap ->
                    LaporanPenjualanCard(pesananLengkap)
                }
            }
        }
    }
}

@Composable
fun LaporanPenjualanCard(pesananLengkap: PesananLengkap) {
    // Data Items sudah ada di PesananLengkap, tidak perlu observe lagi dari ViewModel
    val itemsForPesanan = pesananLengkap.items
    val pesanan = pesananLengkap.pesanan

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
                text = "Pesanan #${pesanan.id.takeLast(8).uppercase()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(modifier = Modifier.height(8.dp))

            // Info user, tanggal & metode pembayaran
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val dateString = formatter.format(pesanan.tanggal.toDate())

            Column(modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text( text = "Customer: ${pesananLengkap.user?.nama ?: "Tidak diketahui"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Tanggal: $dateString", style = MaterialTheme.typography.bodyMedium)
                // Ekspedisi (Nama) belum disimpan di Pesanan Model, jadi sementara hide atau placeholder
                Text(text = "Ekspedisi: -", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Pembayaran: ${pesanan.metodePembayaran.name}", style = MaterialTheme.typography.bodyMedium)
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
                            text = "${item.produkNama} (x${item.jumlah})",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Rp ${String.format("%,.0f", item.produkHarga * item.jumlah)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(modifier = Modifier.height(8.dp))

            // Total harga & jumlah item
            val totalHarga = itemsForPesanan.sumOf { it.produkHarga * it.jumlah }
            val totalItem = itemsForPesanan.sumOf { it.jumlah }
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
                    text = "Total: Rp ${String.format("%,.0f", totalHarga)}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// ... (Bagian DatePickerDialog tetap sama seperti sebelumnya, copy dari kode lama Anda jika belum ada) ...
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