package com.example.penjualan_produk_umkm.auth

// ini Branch Pesanan
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.*
import androidx.core.content.ContextCompat
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.MainActivity
import com.example.penjualan_produk_umkm.OwnerActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.FragmentLoginBinding
import com.example.penjualan_produk_umkm.dummyUsers


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val DEBUG_LOGIN_USER = true
    private val DEBUG_LOGIN_OWNER = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makeRegisterClick()
        binding.loginButton.setOnClickListener {
            performLogin()
        }

        // TODO ini adalah debug
        // Debugging biar langsung login
        if (DEBUG_LOGIN_USER) {
            view.post {
                binding.editTextUsername.setText("user@example.com")
                binding.editTextPassword.setText("123456")
                performLogin()
            }
        }

//        if (DEBUG_LOGIN_OWNER) {
//            view.post {
//                binding.editTextUsername.setText("owner@example.com")
//                binding.editTextPassword.setText("owner123")
//                performLogin()
//            }
//        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun makeRegisterClick(){
        val fullText = getString(R.string.login_hyperlink)
        val spannable = SpannableString(fullText)

        val registerWord = "Register"
        val startIndex = fullText.indexOf(registerWord)
        val endIndex = startIndex + registerWord.length

        if (startIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Ke fragment register
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

    private fun performLogin() {
        val input = binding.editTextUsername.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        val users = dummyUsers

        if (users.isNullOrEmpty()) {
            showToast("No users registered yet")
            return
        }

        // Cari user berdasarkan email (key) atau noTelepon
        val user = users[input] ?: users.values.find { it.noTelepon == input }

        if (user != null && user.password == password) {
            showToast("Login successful!")

            binding.progressBar.visibility = View.VISIBLE
            binding.loginButton.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true

                // Navigate based on role
                val intent = when (user.role) {
                    "owner" -> Intent(requireContext(), OwnerActivity::class.java)
                    else -> Intent(requireContext(), MainActivity::class.java)
                }
                startActivity(intent)
                activity?.finish()
            }, 2000)
        } else {
            showToast("Invalid credentials")
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}