package com.example.penjualan_produk_umkm.owner.dashboard

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

// Buat Kalender
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}
