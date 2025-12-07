package com.example.penjualan_produk_umkm.owner.dashboard.penjualan

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.OwnerPesananViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Penjualan(navController: NavController, viewModel: OwnerPesananViewModel) {

    // Pendapatan kotor
    val pendapatanKotor by viewModel.getPendapatanKotor().observeAsState(0.0)

    // Hasil pendapatan hari ini
    val hasilHariIni by viewModel.getHasilPenjualanHariIni().observeAsState(0.0)

    val scrollState = rememberScrollState()

    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Penjualan") }, navigationIcon = {
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
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ringkasan Kinerja Penjualan",
                        modifier = Modifier.padding(bottom = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )

                    Text(
                        text = "Pendapatan",
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Kotak Pendapatan Kotor
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(180.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = "Money",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Pendapatan Kotor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Normal
                            )

                            Text(
                                text = "Rp ${String.format("%,.0f", pendapatanKotor)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                fontWeight = FontWeight.SemiBold
                            )

                            // Indikator peningkatan/penurunan
                            PeningkatanPendapatan(viewModel = viewModel)
                        }

                        // Kotak Rata-rata Penjualan Harian
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(180.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Daily Sales",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Perubahan Day-over-Day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Normal
                            )

                            Text(
                                text = "Rp ${String.format("%,.0f", hasilHariIni)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                fontWeight = FontWeight.SemiBold
                            )

                            IndikatorPenjualanHariIni(viewModel = viewModel)
                        }


                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Transaksi harian",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "See More",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { navController.navigate("keuangan") })
                        }


                        GrafikTransaksi(viewModel = viewModel)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Text(
                            text = "Produk Terlaris",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        TopProdukTerjual(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun PeningkatanIndikator(persen: Float) {
    val isUp = persen >= 0
    val bgColor = if (isUp) Color(0xFF4CAF50) else Color(0xFFF44336)
    val icon = if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val text = if (isUp) "+${persen.toInt()}%" else "${persen.toInt()}%"

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (isUp) "Up" else "Down",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PeningkatanPendapatan(viewModel: OwnerPesananViewModel) {

    val sekarang by viewModel.getPendapatanKotorPeriode(
        startTime = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000,
        endTime = System.currentTimeMillis()
    ).observeAsState(0.0)

    val sebelumnya by viewModel.getPendapatanKotorPeriode(
        startTime = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000,
        endTime = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
    ).observeAsState(0.0)

    val growth = if (sebelumnya > 0) {
        ((sekarang - sebelumnya) / sebelumnya) * 100.0
    } else {
        100.0 // Kalau sebelumnya 0, otomatis dianggap naik signifikan
    }

    PeningkatanIndikator(persen = growth.toFloat())
}


@Composable
fun IndikatorPenjualanHariIni(viewModel: OwnerPesananViewModel) {
    val hariIni by viewModel.getHasilPenjualanHariIni().observeAsState(0.0)
    val kemarin by viewModel.getHasilPenjualanHariKemarin().observeAsState(0.0)

    val persen = hitungPeningkatanPersen(hariIni, kemarin)

    PeningkatanIndikator(persen = persen)
}


fun hitungPeningkatanPersen(hariIni: Double, kemarin: Double): Float {
    return when {
        kemarin == 0.0 && hariIni == 0.0 -> 0f
        kemarin == 0.0 && hariIni > 0.0 -> 100f
        else -> (((hariIni - kemarin) / kemarin) * 100).toFloat()
    }
}

@Composable
fun GrafikTransaksi(viewModel: OwnerPesananViewModel, modifier: Modifier = Modifier) {
    val allData by viewModel.getTransaksiPerHari().observeAsState(emptyList())
    val data = allData.takeLast(5) // ambil 5 hari terakhir

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.setDrawGridLines(true)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                legend.isEnabled = true
                animateY(500)
            }
        },
        update = { chart ->
            if (data.isEmpty()) {
                chart.clear()
                return@AndroidView
            }

            val entries = data.mapIndexed { index, pair ->
                BarEntry(index.toFloat(), pair.second.toFloat())
            }

            val dataSet = BarDataSet(entries, "Transaksi").apply {
                color = 0xFF4CAF50.toInt()
                valueTextColor = 0xFF000000.toInt()
                valueTextSize = 16f
            }

            chart.data = BarData(dataSet).apply { barWidth = 0.6f }

            // X-axis label
            chart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setCenterAxisLabels(true)
                valueFormatter = IndexAxisValueFormatter(data.map { it.first })
                labelRotationAngle = 0f
                textSize = 14f
                yOffset = 10f
                setDrawLabels(true)
                granularity = 1f
                setLabelCount(data.size, true)
            }

            // Y-axis scaling
            val maxY = (data.maxOfOrNull { it.second.toFloat() } ?: 1f) * 1.2f
            chart.axisLeft.axisMinimum = 0f
            chart.axisLeft.axisMaximum = maxY.coerceAtLeast(10f)
            chart.axisLeft.textSize = 14f

            chart.legend.apply {
                textSize = 16f
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
            }

            chart.invalidate()
        }
    )
}

@Composable
fun TopProdukTerjual(viewModel: OwnerPesananViewModel) {
    // Pastikan ViewModel sudah load data
    LaunchedEffect(Unit) {
        viewModel.loadProdukTerjual()
    }

    val produkTerjual by viewModel.produkTerjualList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ),
    ) {
        produkTerjual.forEach { produk ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, start = 16.dp, top = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(0.7f),
                        text = produk.nama,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(30.dp))

                    Column(
                        modifier = Modifier.weight(0.3f),
                    ) {
                        Text(
                            text = "${produk.jumlahTerjual} terjual",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Progress bar
                        val progress = if (produk.stok > 0) {
                            produk.jumlahTerjual.toFloat() / produk.stok.toFloat()
                        } else 1f

                        LinearProgressIndicator(
                            progress = progress.coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }

                }
            }
        }
    }
}
