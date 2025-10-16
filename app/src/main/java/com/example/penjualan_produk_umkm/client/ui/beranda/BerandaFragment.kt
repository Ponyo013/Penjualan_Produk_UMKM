package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.uiComponent.SearchBar


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BerandaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BerandaFragment : Fragment(R.layout.fragment_beranda) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        // Product Card (Sementara ya nanti kita pakai compose)
        val productsContainer = view.findViewById<LinearLayout>(R.id.products_container)
        val productCard = layoutInflater.inflate(R.layout.item_product, productsContainer, false)

        productsContainer.addView(productCard)

        // Search Bar Using Compose
        val composeView = view.findViewById<ComposeView>(R.id.compose_search_bar)
        composeView.setContent {
            SearchBar(
                onSearch = { query ->
                    println("Searching for: $query")
                }
            )
        }

        return view
    }

    private fun setupNavigationClick(view: View, destinationId: Int) {
        view.setOnClickListener {
            findNavController().navigate(destinationId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigationClick(view.findViewById(R.id.notification_icon), R.id.action_berandaFragment_to_notificationFragment)
        setupNavigationClick(view.findViewById(R.id.cart_icon), R.id.action_BerandaFragment_to_CartFragment)
        setupNavigationClick(view.findViewById(R.id.products_container), R.id.action_BerandaFragment_to_detailProdukFragment)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BerandaFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BerandaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}