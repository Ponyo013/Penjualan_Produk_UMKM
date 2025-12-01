package com.example.penjualan_produk_umkm.auth

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.MainActivity
import com.example.penjualan_produk_umkm.OwnerActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginFirebase(email, password)
            } else {
                showToast("Email dan password tidak boleh kosong.")
            }
        }

        makeRegisterClick()
    }

    private fun loginFirebase(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    // Ambil data user dari Firestore
                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            binding.progressBar.visibility = View.GONE
                            binding.loginButton.isEnabled = true
                            if (document.exists()) {
                                val role = document.getString("role") ?: "user"
                                val userPreferences = UserPreferences(requireContext())
                                userPreferences.saveUser(
                                    id = userId,
                                    email = email,
                                    role = role
                                )
                                showToast("Login berhasil!")
                                navigateToNextScreen(role)
                            } else {
                                showToast("Data user tidak ditemukan di Firestore.")
                            }
                        }
                        .addOnFailureListener { e ->
                            binding.progressBar.visibility = View.GONE
                            binding.loginButton.isEnabled = true
                            showToast("Gagal mengambil data user: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true
                showToast("Login gagal: ${e.message}")
            }
    }

    private fun navigateToNextScreen(role: String?) {
        val context = requireContext()
        val intent = when (role) {
            "owner" -> Intent(context, OwnerActivity::class.java)
            else -> Intent(context, MainActivity::class.java)
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun makeRegisterClick() {
        val fullText = getString(R.string.login_hyperlink)
        val spannable = SpannableString(fullText)
        val registerWord = "Register"
        val startIndex = fullText.indexOf(registerWord)
        val endIndex = startIndex + registerWord.length

        if (startIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    (activity as? AuthActivity)?.replaceFragment(RegisterFragment())
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(requireContext(), R.color.Secondary_3)
                    ds.isUnderlineText = true
                }
            }
            spannable.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvNoHaveAccount.text = spannable
            binding.tvNoHaveAccount.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
