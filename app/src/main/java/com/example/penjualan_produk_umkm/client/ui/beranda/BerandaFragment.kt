package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager // Import ini
import androidx.recyclerview.widget.RecyclerView // Import ini
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.uiComponent.SearchBar
import com.example.penjualan_produk_umkm.produkDummyList // Import list data dummy
import android.widget.LinearLayout

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

        // 1. FILTER DATA: Ambil produk dengan terjual > 20
        // Produk yang lolos: Helm Sepeda (30), Botol Minum (45), Lampu Sepeda (22)
        val popularProducts = produkDummyList.filter { it.terjual > 20 }

        // 2. Setup RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_popular_products) // Gunakan ID baru

        // Atur Layout Manager: Grid 2 Kolom
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        // Setup Adapter
        val adapter = ProductAdapter(popularProducts) { productId ->
            // Aksi saat produk diklik: Navigasi ke Detail Produk
            val bundle = Bundle().apply { putInt("productId", productId) }
            findNavController().navigate(R.id.action_BerandaFragment_to_detailProdukFragment, bundle)
        }
        recyclerView.adapter = adapter

        // --- Search Bar (Compose) ---
        val composeView = view.findViewById<ComposeView>(R.id.compose_search_bar)
        composeView.setContent {
            SearchBar(
                onSearch = { query ->
                    // Saat user mengetik, kita navigasikan ke halaman pencarian
                    findNavController().navigate(R.id.action_global_to_searchFragment)
                }
                // Catatan: Anda juga dapat membuat Box di sekitar SearchBar di XML menjadi clickable
                // dan navigasi di setupNavigationClick.
            )
        }

// Navigasi Sparepart
        view.findViewById<LinearLayout>(R.id.cat_sparepart).setOnClickListener {
            // Pastikan string "parentCategoryName" digunakan sebagai kunci Bundle
            val bundle = Bundle().apply { putString("parentCategoryName", "Spare Parts") }
            findNavController().navigate(R.id.action_BerandaFragment_to_categoryListFragment, bundle)
        }

        // Navigasi Aksesori
        view.findViewById<LinearLayout>(R.id.cat_aksesoris).setOnClickListener {
            val bundle = Bundle().apply { putString("parentCategoryName", "Aksesoris") }
            findNavController().navigate(R.id.action_BerandaFragment_to_categoryListFragment, bundle)
        }

        // Navigasi Sepeda
        view.findViewById<LinearLayout>(R.id.cat_sepeda_induk).setOnClickListener {
            val bundle = Bundle().apply { putString("parentCategoryName", "Sepeda") }
            findNavController().navigate(R.id.action_BerandaFragment_to_categoryListFragment, bundle)
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