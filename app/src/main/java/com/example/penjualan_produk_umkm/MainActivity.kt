package com.example.penjualan_produk_umkm

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.widget.*
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.core.view.WindowInsetsCompat
import com.example.penjualan_produk_umkm.R


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val berandaIcon = findViewById<ImageButton>(R.id.beranda_icon)
        val pesananIcon = findViewById<ImageButton>(R.id.pesanan_icon)
        val profilIcon = findViewById<ImageButton>(R.id.profil_icon)
        val pusatBantuanIcon = findViewById<ImageButton>(R.id.pusatbantuan_icon)

        setSelectedIcon(berandaIcon)

        berandaIcon.setOnClickListener {
            navController.navigate(R.id.BerandaFragment)
            setSelectedIcon(berandaIcon)
        }
        pesananIcon.setOnClickListener {
            navController.navigate(R.id.pesananFragment)
            setSelectedIcon(pesananIcon)
        }
        profilIcon.setOnClickListener {
            navController.navigate(R.id.profileFragment)
            setSelectedIcon(profilIcon)
        }
        pusatBantuanIcon.setOnClickListener {
            navController.navigate(R.id.pusatBantuanFragment)
            setSelectedIcon(pusatBantuanIcon)
        }
    }

    private fun setSelectedIcon(selected: ImageButton) {
        val buttons = listOf(
            findViewById<ImageButton>(R.id.beranda_icon),
            findViewById(R.id.pesanan_icon),
            findViewById(R.id.profil_icon),
            findViewById(R.id.pusatbantuan_icon)
        )
        buttons.forEach { it.isSelected = it == selected }
    }
}