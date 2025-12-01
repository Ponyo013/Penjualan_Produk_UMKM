package com.example.penjualan_produk_umkm.client.ui.search

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.client.ui.beranda.ProductAdapter
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.uiComponent.SearchBar
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import java.util.Locale
import androidx.fragment.app.viewModels

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchAdapter: ProductAdapter

    // Gunakan ViewModel yang sudah dimigrasi (Diasumsikan sudah tidak butuh DAO)
    private val viewModel: ProdukViewModel by viewModels()

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
                currentQuery = query // Simpan query terbaru
                applyFilters()
            })
        }

        // Price filter setup
        val etHargaMin = view.findViewById<EditText>(R.id.et_harga_min)
        val etHargaMax = view.findViewById<EditText>(R.id.et_harga_max)
        val btnApplyFilter = view.findViewById<Button>(R.id.btn_apply_filter)

        // RecyclerView
        recyclerView = view.findViewById(R.id.recycler_search_results)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // FIX: Adapter sekarang menerima String ID
        searchAdapter = ProductAdapter(emptyList()) { productId ->
            val bundle = Bundle().apply { putString("productId", productId) }
            findNavController().navigate(R.id.action_global_to_detailProdukFragment, bundle)
        }
        recyclerView.adapter = searchAdapter

        btnApplyFilter.setOnClickListener {
            minPrice = etHargaMin.text.toString().toDoubleOrNull() ?: 0.0
            maxPrice = etHargaMax.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
            applyFilters() // Panggil filter saat tombol diklik
        }

        // Amati data produk dari ViewModel (setelah query dijalankan)
        viewModel.allProduk.observe(viewLifecycleOwner) { produkList ->
            // Update adapter dengan data yang sudah difilter
            // Note: Data yang datang dari ViewModel harusnya sudah di-filter di sana
            applyFilters(produkList)
        }

        // Muat produk awal
        viewModel.getAllProduk()
    }

    // Fungsi ini sekarang hanya menangani filter UI lokal (setelah data diambil)
    private fun applyFilters(allProduk: List<Produk>? = null) {
        val listToFilter = allProduk ?: viewModel.allProduk.value ?: emptyList()

        val filteredList = listToFilter.filter { produk ->
            val matchesQuery = produk.nama.lowercase(Locale.getDefault()).contains(currentQuery.lowercase(Locale.getDefault())) ||
                    produk.deskripsi.lowercase(Locale.getDefault()).contains(currentQuery.lowercase(Locale.getDefault()))

            val matchesPrice = produk.harga in minPrice..maxPrice
            val matchesInitialCategory = categoryFilterQuery == null ||
                    produk.kategori.equals(categoryFilterQuery, true)

            // Kita asumsikan currentCategory diabaikan untuk simplifikasi

            matchesQuery && matchesPrice && matchesInitialCategory
        }

        searchAdapter.updateProducts(filteredList)
    }
}