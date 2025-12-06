package com.example.penjualan_produk_umkm.owner.dashboard.produkScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.penjualan_produk_umkm.owner.dashboard.Review
import com.example.penjualan_produk_umkm.owner.dashboard.SentimentStats
import com.example.penjualan_produk_umkm.ml.SentimentAnalyzer
import com.example.penjualan_produk_umkm.owner.dashboard.TextPreprocessor
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentimentDetailScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    // States
    var analyzer: SentimentAnalyzer? by remember { mutableStateOf(null) }
    var modelStatus by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }

    var todayStats by remember { mutableStateOf(SentimentStats()) }
    var weekStats by remember { mutableStateOf(SentimentStats()) }
    var allTimeStats by remember { mutableStateOf(SentimentStats()) }

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var selectedPeriod by remember { mutableStateOf("today") }

    // Initialize
    DisposableEffect(Unit) {
        try {
            val textPreprocessor = TextPreprocessor(context)
            analyzer = SentimentAnalyzer(context, textPreprocessor)

            // Auto load on start
            loadAndAnalyzeReviews(
                firestore = firestore,
                analyzer = analyzer,
                onStart = { isAnalyzing = true },
                onComplete = { analyzedReviews, today, week, all ->
                    reviews = analyzedReviews
                    todayStats = today
                    weekStats = week
                    allTimeStats = all
                    isAnalyzing = false
                },
                onError = { e ->
                    modelStatus = "Error: ${e.message}"
                    isAnalyzing = false
                },
                scope = coroutineScope
            )
        } catch (e: Exception) {
            modelStatus = "Error: ${e.message}"
        }

        onDispose {
            analyzer?.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sentimen Ulasan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            loadAndAnalyzeReviews(
                                firestore = firestore,
                                analyzer = analyzer,
                                onStart = { isAnalyzing = true },
                                onComplete = { analyzedReviews, today, week, all ->
                                    reviews = analyzedReviews
                                    todayStats = today
                                    weekStats = week
                                    allTimeStats = all
                                    isAnalyzing = false
                                },
                                onError = { e ->
                                    modelStatus = "Error: ${e.message}"
                                    isAnalyzing = false
                                },
                                scope = coroutineScope
                            )
                        },
                        enabled = !isAnalyzing && analyzer != null
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Status Card - Only show if there is an error
                if (modelStatus.isNotBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = modelStatus,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Period Selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PeriodButton("Hari Ini", "today", selectedPeriod) { selectedPeriod = it }
                        PeriodButton("Minggu Ini", "week", selectedPeriod) { selectedPeriod = it }
                        PeriodButton("Semua", "all", selectedPeriod) { selectedPeriod = it }
                    }
                }

                // Statistics
                item {
                    val currentStats = when (selectedPeriod) {
                        "today" -> todayStats
                        "week" -> weekStats
                        else -> allTimeStats
                    }

                    StatisticsSection(stats = currentStats, period = selectedPeriod)
                }

                // Reviews List
                val filteredReviews = when (selectedPeriod) {
                    "today" -> reviews.filter { isToday(it.tanggal) }
                    "week" -> reviews.filter { isThisWeek(it.tanggal) }
                    else -> reviews
                }

                if (filteredReviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada ulasan untuk periode ini",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(filteredReviews) { review ->
                        ReviewCard(review = review)
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Loading Overlay
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Menganalisis ulasan...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodButton(
    label: String,
    period: String,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    Button(
        onClick = { onPeriodSelected(period) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedPeriod == period)
                MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (selectedPeriod == period)
                MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(label)
    }
}

@Composable
fun StatisticsSection(stats: SentimentStats, period: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(
                title = "Positif",
                count = stats.positif,
                percentage = stats.positifPercent,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            )
            StatCard(
                title = "Netral",
                count = stats.netral,
                percentage = stats.netralPercent,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            )
            StatCard(
                title = "Negatif",
                count = stats.negatif,
                percentage = stats.negatifPercent,
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "%.1f%%".format(percentage),
                fontSize = 12.sp,
                color = color
            )
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = review.produkId,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                SentimentBadge(sentiment = review.sentiment)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = review.komentar,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "★".repeat(review.rating.toInt()) + "☆".repeat(5 - review.rating.toInt()),
                    fontSize = 14.sp,
                    color = Color(0xFFFFC107)
                )
                Text(
                    text = formatDate(review.tanggal),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "Confidence: ${"%.1f".format(review.sentimentConfidence * 100)}%",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SentimentBadge(sentiment: String) {
    val (color, label) = when (sentiment.lowercase()) {
        "positif" -> Color(0xFF4CAF50) to "😊 Positif"
        "negatif" -> Color(0xFFF44336) to "😞 Negatif"
        else -> Color(0xFFFF9800) to "😐 Netral"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// Helper functions
private fun loadAndAnalyzeReviews(
    firestore: FirebaseFirestore,
    analyzer: SentimentAnalyzer?,
    onStart: () -> Unit,
    onComplete: (List<Review>, SentimentStats, SentimentStats, SentimentStats) -> Unit,
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
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .limit(100)
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
                                Log.e("SentimentDetailScreen", "Gagal mem-parsing tanggal: $tanggalValue", e)
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
                        userName = doc.getString("userName") ?: "Anonim",
                        rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                        komentar = doc.getString("komentar") ?: "",
                        tanggal = timestamp
                    )
                } catch (e: Exception) {
                    Log.e("SentimentDetailScreen", "Error parsing review doc ${doc.id}", e)
                    null
                }
            }

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

            val todayStats = calculateStats(analyzedReviews.filter { isToday(it.tanggal) })
            val weekStats = calculateStats(analyzedReviews.filter { isThisWeek(it.tanggal) })
            val allTimeStats = calculateStats(analyzedReviews)

            withContext(Dispatchers.Main) {
                onComplete(analyzedReviews, todayStats, weekStats, allTimeStats)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e)
            }
            e.printStackTrace()
        }
    }
}

private fun calculateStats(reviews: List<Review>): SentimentStats {
    return SentimentStats(
        positif = reviews.count { it.sentiment.lowercase() == "positif" },
        negatif = reviews.count { it.sentiment.lowercase() == "negatif" },
        netral = reviews.count { it.sentiment.lowercase() == "netral" },
        total = reviews.size
    )
}

private fun isToday(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_YEAR)
    val todayYear = calendar.get(Calendar.YEAR)

    calendar.timeInMillis = timestamp
    val reviewDay = calendar.get(Calendar.DAY_OF_YEAR)
    val reviewYear = calendar.get(Calendar.YEAR)

    return today == reviewDay && todayYear == reviewYear
}

private fun isThisWeek(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    val thisWeek = calendar.get(Calendar.WEEK_OF_YEAR)
    val thisYear = calendar.get(Calendar.YEAR)

    calendar.timeInMillis = timestamp
    val reviewWeek = calendar.get(Calendar.WEEK_OF_YEAR)
    val reviewYear = calendar.get(Calendar.YEAR)

    return thisWeek == reviewWeek && thisYear == reviewYear
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}
