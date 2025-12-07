package com.example.penjualan_produk_umkm.owner.dashboard.penjualan

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.OwnerPesananViewModel
import com.example.penjualan_produk_umkm.viewModel.PesananLengkap
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Keuangan(navController: NavController, viewModel: OwnerPesananViewModel) {

    // Load semua pesanan (realtime dari Firestore)
    val pesananList by viewModel.pesananList.collectAsState(initial = emptyList())

    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Detail Transaksi") }, navigationIcon = {
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
                    modifier = Modifier.wrapContentWidth().weight(2f)
                ) {
                    Text(tab)
                }
            } else {
                OutlinedButton(
                    onClick = { selectedTab = tab },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.wrapContentWidth().weight(2f)
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
        val isValidStatus =
            status == StatusPesanan.DIKIRIM.name || status == StatusPesanan.SELESAI.name

        if (!isValidStatus) return@filter false

        // Konversi Tanggal
        val date = try {
            val timestamp = pesananLengkap.pesanan.tanggal.toDate()
            val cal = Calendar.getInstance()
            cal.time = timestamp
            LocalDate.of(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
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
                items(list) { pesananLengkap ->
                    LaporanPenjualanCardExpandable(pesananLengkap)
                }
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
    Dialog(
        onDismissRequest = onDismissRequest,
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
                                val selectedDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
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
                            val selectedDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanPenjualanCardExpandable(pesananLengkap: PesananLengkap) {
    val pesanan = pesananLengkap.pesanan
    var expanded by remember { mutableStateOf(false) }
    val itemsForPesanan = pesananLengkap.items

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageModel = itemsForPesanan.firstOrNull()?.gambarUrl ?: ""
                AsyncImage(
                    model = imageModel.ifEmpty { R.drawable.ic_error_image },
                    contentDescription = "Gambar Produk",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${pesanan.id.take(5).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Customer: ${pesananLengkap.user?.nama ?: "-"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${itemsForPesanan.size} item",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Detail (hanya muncul jika expanded = true)
            if (expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    itemsForPesanan.forEach { item ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Metode Pembayaran: ${pesanan.metodePembayaran}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Expedisi: ${pesananLengkap.ekspedisi?.nama ?: "-"}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "Alamat: ${pesananLengkap.user?.alamat ?: "-"}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Text(
                            text = "Detail Pesanan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Nama produk
                            Text(
                                modifier = Modifier.weight(2f),
                                text = "${item.produkNama}\n(Rp ${
                                    String.format(
                                        "%,.0f",
                                        item.produkHarga
                                    )
                                })",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                            )

                            // Jumlah
                            Text(
                                modifier = Modifier.weight(0.3f),
                                text = "X${item.jumlah}",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )

                            // Harga total
                            Text(
                                modifier = Modifier.weight(1f),
                                text = "Rp ${
                                    String.format(
                                        "%,.0f",
                                        item.produkHarga * item.jumlah
                                    )
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    val totalHarga = itemsForPesanan.sumOf { it.produkHarga * it.jumlah }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rp ${String.format("%,.0f", totalHarga)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
