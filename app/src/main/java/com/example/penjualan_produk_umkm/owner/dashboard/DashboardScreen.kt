package com.example.penjualan_produk_umkm.owner.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.auth.UserPreferences
// 2. HAPUS IMPORT DATABASE LAMA
// import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.DashboardViewModel
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// 3. PERBAIKI INISIALISASI VIEWMODEL (Hapus parameter db)
fun DashboardScreen(navController: NavController, dashboardViewModel: DashboardViewModel = viewModel(
    factory = ViewModelFactory() // Tidak perlu db = ... lagi karena sudah pakai Firestore
)) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    // 4. GUNAKAN collectAsState UNTUK STATEFLOW
    val allPesanan by dashboardViewModel.allPesanan.collectAsState(initial = emptyList())

    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.notification_icon),
                                    contentDescription = "Icon Notifikasi"
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .clickable { showLogoutDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = "Icon Logout",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (showLogoutDialog) {
                                AlertDialog(
                                    onDismissRequest = { showLogoutDialog = false },
                                    title = { Text("Konfirmasi Logout", fontWeight = FontWeight.Bold) },
                                    text = { Text("Apakah kamu yakin ingin keluar dari akun ini?") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showLogoutDialog = false
                                            val prefs = UserPreferences(context)
                                            prefs.clear()
                                            val intent = Intent(context, AuthActivity::class.java).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            }
                                            context.startActivity(intent)
                                            (context as? Activity)?.finish()
                                        }) {
                                            Text("Logout", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showLogoutDialog = false }) {
                                            Text("Batal")
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
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

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 5. FILTER TANGGAL YANG AMAN
                val pesananBulanIni = allPesanan.filter {
                    val timestamp = it.tanggal
                    if (timestamp != null) {
                        val date = timestamp.toDate()
                        val cal = Calendar.getInstance()
                        cal.time = date
                        val month = cal.get(Calendar.MONTH) + 1
                        val year = cal.get(Calendar.YEAR)

                        val today = LocalDate.now()
                        month == today.monthValue && year == today.year
                    } else {
                        false
                    }
                }

                RingkasanOmsetPesanan(pesananList = pesananBulanIni)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Chevron Icon",
                                modifier = Modifier.clickable { navController.navigate("listPesanan") }
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatusKategoriList(pesananList = allPesanan)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Buttons(navController = navController as NavHostController)
                }
            }
        }
    }
}

// ... (Kode Tanggal, Buttons, OptButton TETAP SAMA, tidak perlu diubah) ...
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
                        "Produk" -> navController.navigate("produkManage")
                        "Keuangan" -> navController.navigate("Keuangan")
                        "Expedisi" -> navController.navigate("expedisi")
                        "Kirim" -> navController.navigate("kirimNotifikasi")
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
    val totalOmset = pesananList
        .filter {
            // Pastikan membandingkan String dengan String
            it.status != StatusPesanan.DIBATALKAN.name &&
                    it.status != "KERANJANG"
        }
        .sumOf { it.totalHarga }

    val jumlahPesanan = pesananList.size
    val formattedOmset = "Rp " + String.format(Locale("id", "ID"), "%,.0f", totalOmset).replace(',', '.')

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Omset", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(text = formattedOmset, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Tanggal()
                Text(text = "$jumlahPesanan pesanan", style = MaterialTheme.typography.bodyMedium.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun StatusKategoriList(pesananList: List<Pesanan>) {
    val kategoriList = listOf(
        Triple("Proses", Icons.Filled.AccessTime, pesananList.count { it.status == StatusPesanan.DIPROSES.name }),
        Triple("Kirim", Icons.Outlined.LocalShipping, pesananList.count { it.status == StatusPesanan.DIKIRIM.name }),
        Triple("Selesai", Icons.Filled.CheckCircle, pesananList.count { it.status == StatusPesanan.SELESAI.name }),
        Triple("Batal", Icons.Filled.Cancel, pesananList.count { it.status == StatusPesanan.DIBATALKAN.name })
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        kategoriList.forEach { (label, icon, jumlah) ->
            StatusItem(icon = icon, label = label, count = jumlah)
        }
    }
}

@Composable
fun StatusItem(icon: ImageVector, label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Text(text = "$label: $count", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
    }
}

@Preview
@Composable
fun DashboardScreenPreview() {
    // Preview requires mock viewmodel, skipping for now
}