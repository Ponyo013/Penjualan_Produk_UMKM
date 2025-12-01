package com.example.penjualan_produk_umkm.auth

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
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        makeLoginLink()

        binding.btnRegister.setOnClickListener {
            val username = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
            val noPattern = "08\\d{8,11}".toRegex()

            when {
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    showToast("Please fill all fields")
                }
                !username.matches(emailPattern) && !username.matches(noPattern) -> {
                    showToast("Username must be a valid email or Indonesian phone number")
                }
                password != confirmPassword -> {
                    showToast("Passwords do not match")
                }
                else -> {
                    val name = if (username.contains("@")) {
                        username.substringBefore("@").replaceFirstChar { it.uppercase() }
                    } else username

                    val email = if (username.matches(emailPattern)) username else ""
                    val noTelepon = if (username.matches(noPattern)) username else ""

                    registerFirebase(name, email, password, noTelepon)
                }
            }
        }
    }

    private fun registerFirebase(name: String, email: String, password: String, noTelepon: String) {
        binding.btnRegister.isEnabled = false

        // Jika email kosong berarti user registrasi dengan no telepon
        if (email.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid
                    if (uid != null) {
                        saveUserToFirestore(uid, name, email, noTelepon)
                    } else {
                        binding.btnRegister.isEnabled = true
                        showToast("Gagal membuat user.")
                    }
                }
                .addOnFailureListener { e ->
                    binding.btnRegister.isEnabled = true
                    showToast("Register gagal: ${e.message}")
                }
        } else {
            // Registrasi dengan nomor telepon bisa menggunakan custom auth / simpan langsung ke Firestore
            val uid = firestore.collection("users").document().id // generate id manual
            saveUserToFirestore(uid, name, email, noTelepon)
        }
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String, noTelepon: String) {
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "noTelepon" to noTelepon,
            "role" to "user"
        )

        firestore.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                binding.btnRegister.isEnabled = true
                showToast("Registrasi berhasil!")
                (activity as? AuthActivity)?.replaceFragment(LoginFragment())
            }
            .addOnFailureListener { e ->
                binding.btnRegister.isEnabled = true
                showToast("Gagal menyimpan data user: ${e.message}")
            }
    }

    private fun makeLoginLink() {
        val fullText = getString(R.string.register_hyperlink)
        val spannable = SpannableString(fullText)
        val loginWord = "Login"
        val startIndex = fullText.indexOf(loginWord)
        val endIndex = startIndex + loginWord.length

        if (startIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    (activity as? AuthActivity)?.replaceFragment(LoginFragment())
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(requireContext(), R.color.Secondary_3)
                    ds.isUnderlineText = true
                }
            }
            spannable.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvAlreadyHaveAccount.text = spannable
            binding.tvAlreadyHaveAccount.movementMethod = LinkMovementMethod.getInstance()
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
