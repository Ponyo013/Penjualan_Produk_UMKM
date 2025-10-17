package com.example.penjualan_produk_umkm.owner.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.dummyPesanan
import com.example.penjualan_produk_umkm.model.Pesanan
import com.example.penjualan_produk_umkm.model.StatusPesanan
import com.example.penjualan_produk_umkm.style.UMKMTheme
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                            // Tombol Logout
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    )
                                    .clickable { showLogoutDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = "Icon Logout",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Dialog konfirmasi logout
                            if (showLogoutDialog) {
                                AlertDialog(
                                    onDismissRequest = { showLogoutDialog = false },
                                    title = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector =  Icons.AutoMirrored.Outlined.Logout,
                                                contentDescription = "Logout Icon",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Text(
                                                text = "Konfirmasi Logout",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    },
                                    text = {
                                        Text(
                                            "Apakah kamu yakin ingin keluar dari akun ini?",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                showLogoutDialog = false
                                                val intent = Intent(context, AuthActivity::class.java)
                                                context.startActivity(intent)
                                                (context as? Activity)?.finish()
                                            }
                                        ) {
                                            Text(
                                                text = "Logout",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showLogoutDialog = false }
                                        ) {
                                            Text(
                                                text = "Batal",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    tonalElevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp)
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
                // Ringkasan Omset Pesanan
                RingkasanOmsetPesanan(
                    pesananList = dummyPesanan.filter {
                        it.tanggal.month == LocalDate.now().month &&
                                it.tanggal.year == LocalDate.now().year
                    }
                )

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
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.clickable {
                                    navController.navigate("listPesanan")
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusKategoriList( pesananList = dummyPesanan)
                        }
                    }
                }

                // Grafik penjualan/Jumlah Pengunjung/Jumlah Produk terjual/Jumlah ulasan baru/ Rata rata rating toko

                // Buttons - Produk, Keuangan, Peforma Toko
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Buttons(navController = navController as NavHostController)
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
fun Buttons(navController: NavHostController){
    val buttonKategori = listOf(
        "Produk" to Icons.Filled.AllInbox,
        "Keuangan" to Icons.Outlined.MonetizationOn,
        "Kirim" to Icons.Outlined.NotificationAdd,
        "Expedisi" to Icons.Outlined.LocalShipping
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            buttonKategori.forEach { (label, ikon) ->
                OptButton(label, ikon) {
                    when (label) {
                        "Produk" -> {
                            navController.navigate("produkManage")
                        }
                        "Keuangan" -> {
                            navController.navigate("Keuangan")
                        }
                        "Expedisi" -> {
                            navController.navigate("expedisi")
                        }
                        "Kirim" -> {
                            navController.navigate("kirimNotifikasi")
                        }
                        else -> ""
                    }
                }
            }
        }
    }
}

@Composable
fun OptButton(label: String, ikon: ImageVector, onClick: () -> Unit){
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .wrapContentWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.wrapContentWidth()
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ikon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .size(28.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun RingkasanOmsetPesanan(pesananList: List<Pesanan>) {
    // Hitung total omset dan jumlah pesanan valid
    val totalOmset = pesananList
        .filter { it.status != StatusPesanan.DIBATALKAN } // exclude yang dibatalkan
        .sumOf { pesanan ->
            pesanan.items.sumOf { item ->
                item.produk.harga * item.jumlah
            }
        }

    val jumlahPesanan = pesananList.size

    // Format angka ke bentuk rupiah
    val formattedOmset = "Rp " + String.format(Locale("id", "ID"), "%,.0f", totalOmset)
        .replace(',', '.')

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
                text = formattedOmset,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Komponen tanggal
                Tanggal()

                Text(
                    text = "$jumlahPesanan pesanan",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
fun StatusKategoriList(pesananList: List<Pesanan>) {
    val kategoriList = listOf(
        Triple("Proses", Icons.Filled.AccessTime, pesananList.count { it.status == StatusPesanan.DIPROSES}),
        Triple("Kirim", Icons.Outlined.LocalShipping, pesananList.count { it.status == StatusPesanan.DIKIRIM }),
        Triple("Selesai", Icons.Filled.CheckCircle, pesananList.count { it.status == StatusPesanan.SELESAI }),
        Triple("Batal", Icons.Filled.Cancel, pesananList.count { it.status == StatusPesanan.DIBATALKAN })
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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(28.dp)
        )
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
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

