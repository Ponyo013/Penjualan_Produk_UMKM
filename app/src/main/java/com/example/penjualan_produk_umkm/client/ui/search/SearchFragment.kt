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
import com.example.penjualan_produk_umkm.database.model.Produk
import com.example.penjualan_produk_umkm.uiComponent.SearchBar
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchAdapter: ProductAdapter
    private lateinit var db: AppDatabase

    private var categoryFilterQuery: String? = null
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryFilterQuery = arguments?.getString("categoryQuery")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar_search)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Setup Compose SearchBar
        val composeSearchBar = view.findViewById<ComposeView>(R.id.compose_search_bar_full)
        composeSearchBar.setContent {
            SearchBar(onSearch = { query ->
                loadProducts(query, 0.0, Double.MAX_VALUE)
            })
        }

        // Price filter
        val etHargaMin = view.findViewById<EditText>(R.id.et_harga_min)
        val etHargaMax = view.findViewById<EditText>(R.id.et_harga_max)
        val btnApplyFilter = view.findViewById<Button>(R.id.btn_apply_filter)

        // RecyclerView
        recyclerView = view.findViewById(R.id.recycler_search_results)
        recyclerView.layoutManager = LinearLayoutManager(context)
        searchAdapter = ProductAdapter(emptyList()) { productId ->
            // TODO: Navigate to product detail
        }
        recyclerView.adapter = searchAdapter

        btnApplyFilter.setOnClickListener {
            val minPrice = etHargaMin.text.toString().toDoubleOrNull() ?: 0.0
            val maxPrice = etHargaMax.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
            loadProducts("", minPrice, maxPrice)
        }

        // Load products initially
        loadProducts()
    }

    private fun loadProducts(
        query: String = "",
        minPrice: Double = 0.0,
        maxPrice: Double = Double.MAX_VALUE
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val produkList: List<Produk> = db.produkDao().getAllProdukOnce() // new DAO function
            val lowerCaseQuery = query.lowercase(Locale.getDefault())

            val filteredList = produkList.filter { produk ->
                val matchesQuery = produk.nama.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        produk.deskripsi.lowercase(Locale.getDefault()).contains(lowerCaseQuery)

                val matchesPrice = produk.harga in minPrice..maxPrice
                val matchesInitialCategory = categoryFilterQuery == null ||
                        produk.kategori.equals(categoryFilterQuery, true)

                val matchesCategoryChip = currentCategory == null ||
                        produk.kategori.equals(currentCategory, true)

                matchesQuery && matchesPrice && matchesInitialCategory && matchesCategoryChip
            }

            withContext(Dispatchers.Main) {
                searchAdapter.updateProducts(filteredList)
            }
        }
    }
}
