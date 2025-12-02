package com.example.penjualan_produk_umkm.client.ui.search

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.client.ui.beranda.ProductAdapter
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.uiComponent.SearchBar
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchAdapter: ProductAdapter

    // Gunakan ViewModel Firebase (Factory Kosong)
    private val viewModel: ProdukViewModel by viewModels {
        ViewModelFactory()
    }

    private var categoryFilterQuery: String? = null
    private var currentQuery: String = ""
    private var minPrice: Double = 0.0
    private var maxPrice: Double = Double.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryFilterQuery = arguments?.getString("categoryQuery")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar_search)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Setup Compose SearchBar
        val composeSearchBar = view.findViewById<ComposeView>(R.id.compose_search_bar_full)
        composeSearchBar.setContent {
            SearchBar(onSearch = { query ->
                currentQuery = query
                applyFilters()
            })
        }

        // Price filter setup
        val etHargaMin = view.findViewById<EditText>(R.id.et_harga_min)
        val etHargaMax = view.findViewById<EditText>(R.id.et_harga_max)
        val btnApplyFilter = view.findViewById<Button>(R.id.btn_apply_filter)

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.recycler_search_results)
        recyclerView.layoutManager = LinearLayoutManager(context)

        searchAdapter = ProductAdapter(emptyList()) { productId ->
            // Navigasi ke detail produk (Kirim ID String)
            val bundle = Bundle().apply { putString("productId", productId) }
            findNavController().navigate(R.id.action_global_to_detailProdukFragment, bundle)
        }
        recyclerView.adapter = searchAdapter

        btnApplyFilter.setOnClickListener {
            minPrice = etHargaMin.text.toString().toDoubleOrNull() ?: 0.0
            maxPrice = etHargaMax.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
            applyFilters()
        }

        // Observasi data dari Firestore
        viewModel.allProduk.observe(viewLifecycleOwner) { produkList ->
            // Setiap kali data dari Firebase masuk/berubah, filter ulang
            applyFilters(produkList)
        }

        // Trigger load data (meskipun init block di VM sudah memanggilnya, aman dipanggil lagi)
        viewModel.getAllProduk()
    }

    // Fungsi filter lokal
    private fun applyFilters(listData: List<Produk>? = null) {
        // Gunakan list yang dikirim, atau ambil dari value terakhir LiveData
        val sourceList = listData ?: viewModel.allProduk.value ?: emptyList()
        val lowerCaseQuery = currentQuery.lowercase(Locale.getDefault())

        val filteredList = sourceList.filter { produk ->
            // Filter Nama/Deskripsi
            val matchesQuery = produk.nama.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    produk.deskripsi.lowercase(Locale.getDefault()).contains(lowerCaseQuery)

            // Filter Harga
            val matchesPrice = produk.harga in minPrice..maxPrice

            // Filter Kategori (jika ada argumen dari navigasi sebelumnya)
            val matchesInitialCategory = categoryFilterQuery == null ||
                    produk.kategori.equals(categoryFilterQuery, true)

            matchesQuery && matchesPrice && matchesInitialCategory
        }

        searchAdapter.updateProducts(filteredList)
    }
}