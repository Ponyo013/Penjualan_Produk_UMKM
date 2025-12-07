package com.example.penjualan_produk_umkm.owner.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.auth.UserPreferences
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.example.penjualan_produk_umkm.ml.SentimentAnalyzer
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.viewModel.DashboardViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel = viewModel(factory = ViewModelFactory())
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    var showLogoutDialog by remember { mutableStateOf(false) }
    val allPesanan by dashboardViewModel.allPesanan.collectAsState(initial = emptyList())

    // Sentiment Analysis States
    var analyzer: SentimentAnalyzer? by remember { mutableStateOf(null) }
    var sentimentStats by remember { mutableStateOf(SentimentStats()) }
    var isLoadingSentiment by remember { mutableStateOf(false) }
    var sentimentError by remember { mutableStateOf<String?>(null) }

    // Initialize Sentiment Analyzer
    DisposableEffect(Unit) {
        try {
            val textPreprocessor = TextPreprocessor(context)
            analyzer = SentimentAnalyzer(context, textPreprocessor)
            // Load sentiment data
            loadSentimentData(
                firestore = firestore,
                analyzer = analyzer,
                onStart = { isLoadingSentiment = true },
                onComplete = { stats ->
                    sentimentStats = stats
                    isLoadingSentiment = false
                    sentimentError = null
                },
                onError = { e ->
                    sentimentError = e.message
                    isLoadingSentiment = false
                },
                scope = coroutineScope
            )
        } catch (e: Exception) {
            sentimentError = "Error loading model: ${e.message}"
        }

        onDispose {
            analyzer?.close()
        }
    }

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
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Filter Pesanan Bulan Ini
                val pesananBulanIni = allPesanan.filter {
                    val timestamp = it.tanggal
                    run {
                        val date = timestamp.toDate()
                        val cal = Calendar.getInstance()
                        cal.time = date
                        val month = cal.get(Calendar.MONTH) + 1
                        val year = cal.get(Calendar.YEAR)

                        val today = LocalDate.now()
                        month == today.monthValue && year == today.year
                    }
                }

                // Omset Card
                RingkasanOmsetPesanan(pesananList = pesananBulanIni, dashboardViewModel)

                // Status Pesanan Card
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

                // Buttons Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Buttons(navController = navController as NavHostController)
                }
                // Sentiment Analysis Card
                SentimentAnalysisCard(
                    stats = sentimentStats,
                    isLoading = isLoadingSentiment,
                    error = sentimentError,
                    onRefresh = {
                        loadSentimentData(
                            firestore = firestore,
                            analyzer = analyzer,
                            onStart = { isLoadingSentiment = true },
                            onComplete = { stats ->
                                sentimentStats = stats
                                isLoadingSentiment = false
                                sentimentError = null
                            },
                            onError = { e ->
                                sentimentError = e.message
                                isLoadingSentiment = false
                            },
                            scope = coroutineScope
                        )
                    },
                    onDetailClick = {
                        navController.navigate("SentimentDetailScreen")
                    }
                )
            }
        }
    }
}

@Composable
fun SentimentAnalysisCard(
    stats: SentimentStats,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onDetailClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analisis Sentimen" ,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isLoading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDetailClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Detail",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            when {
                isLoading -> {
                    // Loading State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Text(
                                text = "Menganalisis ulasan...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                error != null -> {
                    // Error State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                stats.total == 0 -> {
                    // Empty State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Inbox,
                                contentDescription = "Empty",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Belum ada ulasan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                else -> {
                    // Data State
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SentimentStatItem(
                            label = "Positif",
                            count = stats.positif,
                            percentage = stats.positifPercent,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        )
                        SentimentStatItem(
                            label = "Netral",
                            count = stats.netral,
                            percentage = stats.netralPercent,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        )
                        SentimentStatItem(
                            label = "Negatif",
                            count = stats.negatif,
                            percentage = stats.negatifPercent,
                            color = Color(0xFFF44336),
                            modifier = Modifier.weight(1f).padding(start = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "${stats.total} ulasan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SentimentStatItem(
    label: String,
    count: Int,
    percentage: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "%.1f%%".format(percentage),
                fontSize = 11.sp,
                color = color
            )
        }
    }
}

// Helper function to load sentiment data
private fun loadSentimentData(
    firestore: FirebaseFirestore,
    analyzer: SentimentAnalyzer?,
    onStart: () -> Unit,
    onComplete: (SentimentStats) -> Unit,
    onError: (Exception) -> Unit,
    scope: CoroutineScope
) {
    if (analyzer == null) {
        onError(IllegalStateException("Model belum diinisialisasi"))
        return
    }

    onStart()
    scope.launch(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("ulasan")
                .get()
                .await()

            val reviews = snapshot.documents.mapNotNull { doc ->
                try {
                    val tanggalValue = doc.get("tanggal")
                    val timestamp = when (tanggalValue) {
                        is Timestamp -> tanggalValue.toDate().time
                        is String -> {
                            try {
                                val dateFormat = SimpleDateFormat("MMMM d, yyyy 'at' h:mm:ss a z", Locale.ENGLISH)
                                dateFormat.parse(tanggalValue)?.time ?: 0L
                            } catch (e: java.text.ParseException) {
                                Log.e("DashboardScreen", "Gagal mem-parsing tanggal: $tanggalValue", e)
                                0L
                            }
                        }
                        is Long -> tanggalValue
                        else -> 0L
                    }
                    Review(
                        id = doc.id,
                        produkId = doc.getString("produkId") ?: "",
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "Anonymous",
                        rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                        komentar = doc.getString("komentar") ?: "",
                        tanggal = timestamp
                    )
                } catch (e: Exception) {
                    Log.e("DashboardScreen", "Error parsing review doc ${doc.id}", e)
                    null
                }
            }

            // Analyze each review
            val analyzedReviews = reviews.map { review ->
                if (review.komentar.isNotBlank()) {
                    val result = analyzer.predict(review.komentar)
                    review.copy(
                        sentiment = result.label,
                        sentimentConfidence = result.confidence
                    )
                } else {
                    review.copy(sentiment = "Netral", sentimentConfidence = 0f)
                }
            }

            // Calculate statistics
            val stats = SentimentStats(
                positif = analyzedReviews.count { it.sentiment.lowercase() == "positif" },
                negatif = analyzedReviews.count { it.sentiment.lowercase() == "negatif" },
                netral = analyzedReviews.count { it.sentiment.lowercase() == "netral" },
                total = analyzedReviews.size
            )

            withContext(Dispatchers.Main) {
                onComplete(stats)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e)
            }
            e.printStackTrace()
        }
    }
}

@Composable
private fun Tanggal() {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    val formattedDate = dateFormat.format(calendar.time)

    Text(
        text = formattedDate,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    )
}

@Composable
fun Buttons(navController: NavHostController){
    val buttonKategori = listOf(
        "Produk" to Icons.Filled.AllInbox,
        "penjualan" to Icons.Outlined.MonetizationOn,
        "Kirim" to Icons.Outlined.NotificationAdd,
        "Expedisi" to Icons.Outlined.LocalShipping
    )

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            buttonKategori.forEach { (label, ikon) ->
                OptButton(label, ikon) {
                    when (label) {
                        "Produk" -> navController.navigate("produkManage")
                        "penjualan" -> navController.navigate("penjualan")
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
        modifier = Modifier.clickable { onClick() }.wrapContentWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)).padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = ikon, contentDescription = label, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(28.dp))
        }
        Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun RingkasanOmsetPesanan(pesananList: List<Pesanan>, dashboardViewModel: DashboardViewModel) {
    val totalOmset = dashboardViewModel.hitungOmset(pesananList)

    val jumlahPesanan = pesananList.size
    val formattedOmset = "Rp " + String.format(Locale("id", "ID"), "%,.0f", totalOmset).replace(',', '.')

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Omset",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = formattedOmset,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Tanggal()

                Text(
                    text = "$jumlahPesanan pesanan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
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
