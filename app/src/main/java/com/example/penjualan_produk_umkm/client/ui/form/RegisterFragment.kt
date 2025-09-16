package com.example.penjualan_produk_umkm.client.ui.form

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.penjualan_produk_umkm.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

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

    // Input
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: MaterialButton

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol Back
        val toolbar =
            view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        // Log in hyperlink
        val tv = view.findViewById<TextView>(R.id.tvAlreadyHaveAccount)
        val text = getString(R.string.register_hyperlink)
        val spannable = SpannableString(text)

        val start = text.indexOf("Log in")
        val end = start + "Log in".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = Color.BLUE
            }
        }
        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv.text = spannable
        tv.movementMethod = LinkMovementMethod.getInstance()

        // Logic Input
        usernameEditText = view.findViewById(R.id.editTextUsername)
        passwordEditText = view.findViewById(R.id.editTextPassword)
        confirmPasswordEditText = view.findViewById(R.id.editTextConfirmPassword)
        registerButton = view.findViewById(R.id.btnRegister)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            when {
                username.isEmpty() -> {
                    Toast.makeText(requireContext(), "Email harus diisi", Toast.LENGTH_SHORT).show()
                }

                password.isEmpty() -> {
                    Toast.makeText(requireContext(), "Password harus diisi", Toast.LENGTH_SHORT)
                        .show()
                }

                password.length < 8 -> {
                    Toast.makeText(requireContext(), "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
                }

                confirmPassword.isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        "Konfirmasi password harus diisi",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                password != confirmPassword -> {
                    Toast.makeText(requireContext(), "Password tidak sama", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    Toast.makeText(requireContext(), "Registrasi berhasil! Lakukan Login", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
            }
        }
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