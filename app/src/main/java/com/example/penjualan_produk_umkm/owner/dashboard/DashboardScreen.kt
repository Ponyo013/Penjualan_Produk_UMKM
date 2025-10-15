package com.example.penjualan_produk_umkm.owner.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.style.UMKMTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    // Title
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    },

                    // Icon Icon
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.notification_icon),
                                    contentDescription = "Icon Notifikasi"
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    },
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->

            // Content
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Ringkasan omset dan pesanan
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Omset",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Rp 1.250.000",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Ambil Tanggal
                            Tanggal()

                            Text(
                                text = "10 pesanan",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            )
                        }


                    }
                }

                // Card Status Pesanan
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Status Pesanan",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Chevron Icon",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                            
                        ) {
                            StatusKategoriList()
                        }

                    }
                }
            }

        }
    }
}

@Composable
private fun Tanggal() {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val formattedDate = dateFormat.format(calendar.time)

    Text(
        text = "$formattedDate",
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    )
}

@Composable
fun StatusKategoriList() {
    val kategoriList = listOf(
        Triple("Baru", Icons.Filled.AccessTime, 5),
        Triple("Proses", Icons.Outlined.LocalShipping, 3),
        Triple("Selesai", Icons.Filled.CheckCircle, 10),
        Triple("Batal", Icons.Filled.Cancel, 1)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        kategoriList.forEach { (label, icon, jumlah) ->
            StatusItem(
                icon = icon,
                label = label,
                count = jumlah
            )
        }
    }
}

@Composable
fun StatusItem(icon: ImageVector, label: String, count: Int) {
    Column(
        modifier = Modifier.wrapContentWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(28.dp)
                .padding(bottom = 4.dp)
        )
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}


@Preview
@Composable
fun DashboardScreenPreview() {
    val fakeNavController = rememberNavController()
    DashboardScreen(navController = fakeNavController)
}
