package com.example.penjualan_produk_umkm

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val beranda = findViewById<ImageButton>(R.id.beranda_icon)
        val pesanan = findViewById<ImageButton>(R.id.pesanan_icon)
        val profil = findViewById<ImageButton>(R.id.profil_icon)
        val pusatbantuan = findViewById<ImageButton>(R.id.pusatbantuan_icon)

        val buttons = listOf(beranda, pesanan, profil, pusatbantuan)
        beranda.isSelected = true

        beranda.setOnClickListener {
            selectButton(buttons, beranda)
        }
        pesanan.setOnClickListener {
            selectButton(buttons, pesanan)
        }
        profil.setOnClickListener {
            selectButton(buttons, profil)
        }
        pusatbantuan.setOnClickListener {
            selectButton(buttons, pusatbantuan)
        }
    }

    private fun selectButton(buttons: List<ImageButton>, selected: ImageButton) {
        buttons.forEach { it.isSelected = it == selected }
    }
}