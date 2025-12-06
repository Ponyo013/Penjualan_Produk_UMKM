package com.example.penjualan_produk_umkm.client.ui.search

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.client.ui.beranda.ProductAdapter
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.databinding.FragmentSearchBinding
import com.example.penjualan_produk_umkm.uiComponent.SearchBar
import com.example.penjualan_produk_umkm.utils.SearchDataManager
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import java.util.Locale

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProdukViewModel by viewModels { ViewModelFactory() }
    private lateinit var dataManager: SearchDataManager

    // Adapters
    private lateinit var searchResultAdapter: ProductAdapter
    private lateinit var recentSearchAdapter: RecentSearchAdapter
    private lateinit var recentlyViewedAdapter: RecentlyViewedAdapter

    // Filter State
    private var currentQuery: String = ""
    private var minPrice: Double = 0.0
    private var maxPrice: Double = Double.MAX_VALUE
    private var currentSort: String = "TERBARU"
    private var currentCategory: String = "Semua"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)
        dataManager = SearchDataManager(requireContext())

        setupUI()
        setupAdapters()
        loadIdleData()

        // Load All Products (for filtering and recently viewed mapping)
        if (viewModel.allProduk.value.isNullOrEmpty()) {
            viewModel.getAllProduk()
        }

        // Observe data changes to update views
        viewModel.allProduk.observe(viewLifecycleOwner) { list ->
            // Update Recently Viewed (Mapping ID -> Object)
            updateRecentlyViewedList(list)

            // If we are searching, update results
            if (currentQuery.isNotEmpty()) applyFilters(list)
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.composeSearchBarFull.setContent {
            SearchBar(
                onQueryChange = { query ->
                    // Ini dipanggil setiap karakter diketik (REALTIME FILTER)
                    handleSearchInput(query)
                },
                onSearchClicked = { query ->
                    // Ini HANYA dipanggil saat tombol Enter/Search ditekan
                    if (query.isNotBlank()) {
                        dataManager.saveSearchHistory(query) // Simpan History di sini!
                        Toast.makeText(context, "Disimpan ke riwayat", Toast.LENGTH_SHORT).show() // Feedback opsional
                    }
                }
            )
        }
        binding.btnFilter.setOnClickListener {
            val bottomSheet = FilterBottomSheetFragment { sort, cat, min, max ->
                currentSort = sort
                currentCategory = cat
                minPrice = min
                maxPrice = max
                applyFilters()
            }
            bottomSheet.show(parentFragmentManager, "FilterBottomSheet")
        }

        binding.tvClearAll.setOnClickListener {
            dataManager.clearSearchHistory()
            loadIdleData()
        }
    }

    private fun handleSearchInput(query: String) {
        currentQuery = query
        if (query.isNotEmpty()) {
            // Mode: SEARCHING (Tampilkan hasil tapi JANGAN simpan history otomatis)
            binding.layoutIdleState.visibility = View.GONE
            binding.recyclerSearchResults.visibility = View.VISIBLE

            applyFilters() // Filter produk realtime
        } else {
            // Mode: IDLE (Kosong)
            binding.layoutIdleState.visibility = View.VISIBLE
            binding.recyclerSearchResults.visibility = View.GONE
            loadIdleData() // Tampilkan history
        }
    }

    private fun setupAdapters() {
        // 1. Search Results (Grid)
        searchResultAdapter = ProductAdapter(emptyList()) { productId ->
            openDetail(productId)
        }
        binding.recyclerSearchResults.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerSearchResults.adapter = searchResultAdapter

        // 2. Recent Search (Vertical)
        recentSearchAdapter = RecentSearchAdapter(
            mutableListOf(),
            onClick = { query ->
                // Trigger search from history
                // Note: Idealnya update text di searchbar, tapi krn Compose, kita trigger logicnya saja
                handleSearch(query)
                Toast.makeText(context, "Mencari: $query", Toast.LENGTH_SHORT).show()
            },
            onRemove = { query ->
                dataManager.removeSearchItem(query)
                checkHistoryEmpty()
            }
        )
        binding.rvRecentSearch.layoutManager = LinearLayoutManager(context)
        binding.rvRecentSearch.adapter = recentSearchAdapter

        // 3. Recently Viewed (Horizontal)
        recentlyViewedAdapter = RecentlyViewedAdapter(emptyList()) { productId ->
            openDetail(productId)
        }
        binding.rvRecentlyViewed.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentlyViewed.adapter = recentlyViewedAdapter
    }

    private fun handleSearch(query: String) {
        currentQuery = query
        if (query.isNotEmpty()) {
            // Mode: SEARCHING
            binding.layoutIdleState.visibility = View.GONE
            binding.recyclerSearchResults.visibility = View.VISIBLE

            // Save history only if meaningful length
            if(query.length > 2) dataManager.saveSearchHistory(query)

            applyFilters()
        } else {
            // Mode: IDLE
            binding.layoutIdleState.visibility = View.VISIBLE
            binding.recyclerSearchResults.visibility = View.GONE
            loadIdleData()
        }
    }

    private fun loadIdleData() {
        // Load History Text
        val history = dataManager.getSearchHistory()
        recentSearchAdapter.updateData(history)
        checkHistoryEmpty()

        // Load Recently Viewed (Product IDs)
        // Kita perlu menunggu data produk dari ViewModel untuk mapping ID -> Produk Asli
        viewModel.allProduk.value?.let { updateRecentlyViewedList(it) }
    }

    private fun checkHistoryEmpty() {
        if (recentSearchAdapter.itemCount == 0) {
            binding.tvNoHistory.visibility = View.VISIBLE
            binding.rvRecentSearch.visibility = View.GONE
        } else {
            binding.tvNoHistory.visibility = View.GONE
            binding.rvRecentSearch.visibility = View.VISIBLE
        }
    }

    private fun updateRecentlyViewedList(allProducts: List<Produk>) {
        val viewedIds = dataManager.getRecentlyViewedIds()
        // Map IDs to Real Product Objects
        val viewedProducts = viewedIds.mapNotNull { id ->
            allProducts.find { it.id == id }
        }

        if (viewedProducts.isEmpty()) {
            binding.tvNoRecentView.visibility = View.VISIBLE
            binding.rvRecentlyViewed.visibility = View.GONE
        } else {
            binding.tvNoRecentView.visibility = View.GONE
            binding.rvRecentlyViewed.visibility = View.VISIBLE
            recentlyViewedAdapter.updateData(viewedProducts)
        }
    }

    private fun applyFilters(listData: List<Produk>? = null) {
        val sourceList = listData ?: viewModel.allProduk.value ?: emptyList()
        val lowerCaseQuery = currentQuery.lowercase(Locale.getDefault())

        var filteredList = sourceList.filter { produk ->
            val matchesQuery = if (currentQuery.isEmpty()) true else {
                produk.nama.lowercase().contains(lowerCaseQuery)
            }
            val matchesPrice = produk.harga in minPrice..maxPrice
            val matchesCategory = if (currentCategory == "Semua") true else {
                produk.kategori.equals(currentCategory, ignoreCase = true)
            }
            matchesQuery && matchesPrice && matchesCategory
        }

        // Sorting
        filteredList = when (currentSort) {
            "TERLARIS" -> filteredList.sortedByDescending { it.terjual }
            "HARGA_MURAH" -> filteredList.sortedBy { it.harga }
            "HARGA_MAHAL" -> filteredList.sortedByDescending { it.harga }
            else -> filteredList // Terbaru
        }

        searchResultAdapter.updateProducts(filteredList)
    }

    private fun openDetail(productId: String) {
        // Simpan ke Recently Viewed sebelum pindah
        dataManager.saveRecentlyViewed(productId)

        val bundle = Bundle().apply { putString("productId", productId) }
        findNavController().navigate(R.id.action_global_to_detailProdukFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}