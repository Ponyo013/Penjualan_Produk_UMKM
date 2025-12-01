package com.example.penjualan_produk_umkm

import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.penjualan_produk_umkm.auth.LoginFragment
import com.example.penjualan_produk_umkm.auth.UserPreferences

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Debug Auto Login
        val prefs = UserPreferences(this)

        if (prefs.isLoggedIn()) {
            val role = prefs.getUserRole()

            val intent = if (role == "owner") {
                Intent(this, OwnerActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }

            startActivity(intent)
            finish()
            return
        }

        if (savedInstanceState == null) {
            replaceFragment(LoginFragment())
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.authFragmentContainer, fragment)
            .commit()
    }
}
