package com.example.penjualan_produk_umkm.auth

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.*
import androidx.core.content.ContextCompat
import com.example.penjualan_produk_umkm.AuthActivity
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.FragmentRegisterBinding
import com.example.penjualan_produk_umkm.dummyUsers
import com.example.penjualan_produk_umkm.model.User

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment(R.layout.fragment_register) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

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
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        makeLoginLink()

        // Logika Data dummy untuk register (passing dari auth)
        val dummyUsers = dummyUsers

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
                dummyUsers?.containsKey(username) == true -> {
                    showToast("User already registered")
                }
                else -> {
                    dummyUsers?.set(username, User(password, "user"))
                    showToast("Registered successfully!")
                    (activity as? AuthActivity)?.replaceFragment(LoginFragment())
                }
            }
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
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}