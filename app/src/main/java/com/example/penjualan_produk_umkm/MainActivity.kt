package com.example.penjualan_produk_umkm

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavContainer: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavContainer = findViewById(R.id.bottom_nav_container)

        val berandaIcon = findViewById<ImageView>(R.id.beranda_icon)
        val pesananIcon = findViewById<ImageView>(R.id.pesanan_icon)
        val profilIcon = findViewById<ImageView>(R.id.profil_icon)

        berandaIcon.setOnClickListener { 
            navController.navigate(R.id.BerandaFragment)
        }
        pesananIcon.setOnClickListener { 
            navController.navigate(R.id.pesananFragment)
        }
        profilIcon.setOnClickListener { 
            navController.navigate(R.id.profileFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val showBottomNav = arguments?.getBoolean("showBottomNav", false) ?: false
            bottomNavContainer.visibility = if (showBottomNav) View.VISIBLE else View.GONE
            
            when (destination.id) {
                R.id.BerandaFragment -> updateBottomNav(berandaIcon)
                R.id.pesananFragment -> updateBottomNav(pesananIcon)
                R.id.profileFragment -> updateBottomNav(profilIcon)
            }
        }

        // Set initial state
        updateBottomNav(berandaIcon)
    }

    private fun updateBottomNav(selectedIcon: ImageView) {
        val allIcons = listOf(
            findViewById<ImageView>(R.id.beranda_icon),
            findViewById<ImageView>(R.id.pesanan_icon),
            findViewById<ImageView>(R.id.profil_icon)
        )

        allIcons.forEach { icon ->
            icon.alpha = if (icon == selectedIcon) 1.0f else 0.5f
        }
    }
}