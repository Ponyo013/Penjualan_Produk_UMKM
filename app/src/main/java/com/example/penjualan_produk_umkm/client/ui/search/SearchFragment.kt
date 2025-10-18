package com.example.penjualan_produk_umkm.client.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.example.penjualan_produk_umkm.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.produkDummyList
import com.example.penjualan_produk_umkm.client.ui.beranda.ProductAdapter
import androidx.compose.ui.platform.ComposeView
import android.widget.EditText
import android.widget.Button
import java.util.Locale
import com.example.penjualan_produk_umkm.uiComponent.SearchBar

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchAdapter: ProductAdapter

    private var categoryFilterQuery: String? = null
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ambil kategoriQuery dari Bundle
        categoryFilterQuery = arguments?.getString("categoryQuery")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

// 1. Setup Search Bar (Compose)
        val composeSearchBar = view.findViewById<ComposeView>(R.id.compose_search_bar_full)
        composeSearchBar.setContent {
            // Error @Composable dihilangkan karena setContent berada di luar scope Composable
            // Gunakan SearchBar yang sudah ada, dengan logika pencarian
            SearchBar(onSearch = { query ->
                performSearchAndFilter(query, 0.0, Double.MAX_VALUE) // Default filter
            })
        }

        // 2. Setup Filter Inputs (Contoh: EditText untuk Harga Min & Maks)
        val etHargaMin = view.findViewById<EditText>(R.id.et_harga_min)
        val etHargaMax = view.findViewById<EditText>(R.id.et_harga_max)
        val btnApplyFilter = view.findViewById<Button>(R.id.btn_apply_filter)

        // 3. Setup RecyclerView untuk Hasil
        recyclerView = view.findViewById(R.id.recycler_search_results)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Inisialisasi adapter dengan semua data (sebelum difilter)
        searchAdapter = ProductAdapter(produkDummyList.toList()) { productId ->
            // Navigasi ke Detail Produk
            // findNavController().navigate(...)
        }
        recyclerView.adapter = searchAdapter

        // 4. Listener Tombol Filter
        btnApplyFilter.setOnClickListener {
            val minPrice = etHargaMin.text.toString().toDoubleOrNull() ?: 0.0
            val maxPrice = etHargaMax.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
            // Ambil query dari SearchBar jika ada (ini rumit di View-Compose Interop)
            val currentQuery = " " // Asumsi query kosong untuk saat ini

            performSearchAndFilter(currentQuery, minPrice, maxPrice)
        }

        // Tampilkan semua produk saat pertama kali dibuka
        performSearchAndFilter("", 0.0, Double.MAX_VALUE)
    }

    // Fungsi Kritis: Melakukan Pencarian dan Filter
    private fun performSearchAndFilter(query: String, minPrice: Double, maxPrice: Double) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        val filteredList = produkDummyList.filter { produk ->

            // --- KOREKSI: DEFINISIKAN matchesQuery dan matchesPrice DI SINI ---
            val matchesQuery = produk.nama.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    produk.deskripsi.lowercase(Locale.getDefault()).contains(lowerCaseQuery)

            val matchesPrice = produk.harga >= minPrice && produk.harga <= maxPrice
            // --- AKHIR DEFINISI ---

            // --- Logika Filter Kategori Awal ---
            val matchesInitialCategory = categoryFilterQuery == null ||
                    produk.kategori.equals(categoryFilterQuery, true)

            // --- Logika Filter Kategori yang Diklik User (Chips) ---
            val matchesCategoryChip = currentCategory == null ||
                    produk.kategori.equals(currentCategory, true)

            // Gabungkan semua filter
            matchesQuery && matchesPrice && matchesInitialCategory && matchesCategoryChip // <-- Sekarang dikenali
        }

        // Update Adapter dengan hasil baru
        searchAdapter.updateProducts(filteredList)
        // (Anda harus menambahkan fun updateProducts di ProductAdapter.kt)
    }
}