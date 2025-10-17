package com.example.penjualan_produk_umkm.owner.dashboard

import android.Manifest
import com.example.penjualan_produk_umkm.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.penjualan_produk_umkm.style.UMKMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KirimNotifikasiScreen(navController: NavController) {
    var judul by remember { mutableStateOf("") }
    var pesan by remember { mutableStateOf("") }
    var terkirim by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Buat Notification Channel (sekali saja)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "umkm_channel"
            val name = "Notifikasi UMKM"
            val descriptionText = "Channel untuk notifikasi umum"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Launcher untuk request permission Android 13+
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                sendNotificationSafe(context, judul, pesan)
                terkirim = true
            } else {
                Toast.makeText(context, "Permission notifikasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    )

    UMKMTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Kirim Notifikasi") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Kirim Notifikasi Umum ke Semua User",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )

                OutlinedTextField(
                    shape = RoundedCornerShape(12.dp),
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul Notifikasi") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    shape = RoundedCornerShape(12.dp),
                    value = pesan,
                    onValueChange = { pesan = it },
                    label = { Text("Isi Pesan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                )

                Button(
                    onClick = {
                        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else true

                        if (permissionGranted) {
                            sendNotificationSafe(context, judul, pesan)
                            terkirim = true
                        } else {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Kirim Notifikasi")
                }

                if (terkirim) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Notifikasi Terkirim!", fontWeight = FontWeight.Bold)
                            Text("Judul: $judul")
                            Text("Pesan: $pesan")
                        }
                    }
                }
            }
        }
    }
}

// Helper function untuk kirim notifikasi dengan handling SecurityException
fun sendNotificationSafe(context: Context, judul: String, pesan: String) {
    try {
        val builder = NotificationCompat.Builder(context, "umkm_channel")
            .setSmallIcon(R.drawable.logo) // Bisa diganti icon app
            .setContentTitle(judul.ifBlank { "Notifikasi dari DWI USAHA" })
            .setContentText(pesan.ifBlank { "Pesan kosong" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(pesan.ifBlank { "Pesan kosong" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_blue_light))
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        NotificationManagerCompat.from(context).notify(1, builder.build())
    } catch (e: SecurityException) {
        e.printStackTrace()
        Toast.makeText(context, "Gagal mengirim notifikasi: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}