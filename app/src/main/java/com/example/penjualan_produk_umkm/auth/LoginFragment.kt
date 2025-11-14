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
import androidx.lifecycle.ViewModelProvider
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.MainActivity
import com.example.penjualan_produk_umkm.OwnerActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.viewModel.LoginViewModel
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val userDao = AppDatabase.getDatabase(requireContext()).userDao()
        val viewModelFactory = ViewModelFactory(userDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                showToast("Email dan password tidak boleh kosong.")
            }
        }

        makeRegisterClick()
    }

    private fun setupObservers() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false
                }

                is LoginViewModel.LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true

                    showToast("Login berhasil!")

                    // Menyimpan user state yang telah login
                    val userPreferences = UserPreferences(requireContext())

                    userPreferences.saveUser(
                        id = state.user.id,
                        email = state.user.email,
                        role = state.user.role.ifEmpty { "user" }
                    )

                    val role = state.user.role.ifEmpty { "user" }
                    navigateToNextScreen(role)
                }

                is LoginViewModel.LoginState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    showToast(state.message)
                }

                else -> {
                    // Optional safeguard
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                }
            }
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
