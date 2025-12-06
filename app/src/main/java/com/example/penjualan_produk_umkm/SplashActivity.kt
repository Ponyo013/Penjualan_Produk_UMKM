package com.example.penjualan_produk_umkm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.penjualan_produk_umkm.utils.DataSeeder

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        createNotificationChannel()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }, 2500)
        val prefs = getSharedPreferences("seeder_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply() // <--- INI MENGHAPUS SEMUA STATUS SEEDING
        DataSeeder.seedProducts(this)
    }


    private fun createNotificationChannel() {
        val channelId = "umkm_channel"
        val channelName = "Notifikasi UMKM"
        val channelDescription = "Channel untuk notifikasi umum di aplikasi UMKM"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}