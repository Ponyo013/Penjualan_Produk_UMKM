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
import com.example.penjualan_produk_umkm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            if (navController.currentDestination?.id == R.id.BerandaFragment) {
                v.setPadding(0, 0, 0, systemBars.bottom)
            } else {
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            }
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.berandaIcon.setOnClickListener { navController.navigate(R.id.BerandaFragment) }
        binding.pesananIcon.setOnClickListener { navController.navigate(R.id.pesananFragment) }
        binding.profilIcon.setOnClickListener { navController.navigate(R.id.profileFragment) }

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val showBottomNav = arguments?.getBoolean("showBottomNav", false) ?: true
            binding.bottomNavContainer.visibility = if (showBottomNav) View.VISIBLE else View.GONE

            when (destination.id) {
                R.id.BerandaFragment -> updateBottomNav(binding.berandaIcon)
                R.id.pesananFragment -> updateBottomNav(binding.pesananIcon)
                R.id.profileFragment -> updateBottomNav(binding.profilIcon)
                else -> {
                    binding.bottomNavContainer.visibility = View.GONE
                }
            }
        }
        updateBottomNav(binding.berandaIcon)
    }

    private fun updateBottomNav(selectedIcon: ImageView) {
        val allIcons = listOf(
            binding.berandaIcon,
            binding.pesananIcon,
            binding.profilIcon
        )
        allIcons.forEach { icon ->
            icon.alpha = if (icon == selectedIcon) 1.0f else 0.5f
        }
    }
}