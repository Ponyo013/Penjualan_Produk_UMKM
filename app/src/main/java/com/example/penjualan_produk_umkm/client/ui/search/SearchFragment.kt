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
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.databinding.FragmentSearchBinding
import com.example.penjualan_produk_umkm.uiComponent.SearchBar
import com.example.penjualan_produk_umkm.utils.SearchDataManager
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import java.util.Locale

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProdukViewModel by viewModels { ViewModelFactory() }
    // TAMBAHAN: CartViewModel
    private val cartViewModel: CartViewModel by viewModels { ViewModelFactory() }

    private lateinit var dataManager: SearchDataManager

    private lateinit var searchResultAdapter: ProductAdapter
    private lateinit var recentSearchAdapter: RecentSearchAdapter
    private lateinit var recentlyViewedAdapter: RecentlyViewedAdapter

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
        showIdleView()

        if (viewModel.allProduk.value.isNullOrEmpty()) {
            viewModel.getAllProduk()
        }

        viewModel.allProduk.observe(viewLifecycleOwner) { list ->
            updateRecentlyViewedList(list)
            if (currentQuery.isNotEmpty() || currentCategory != "Semua") {
                applyFilters(list)
            }
        }
    }

    // ... (setupUI, handleSearchInput, helper view tetap sama) ...
    private fun setupUI() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.composeSearchBarFull.setContent {
            SearchBar(
                readOnly = false, // Mode mengetik
                autoFocus = true, // KEYBOARD MUNCUL OTOMATIS
                onQueryChange = { query ->
                    handleSearchInput(query)
                },
                onSearchClicked = { query ->
                    if (query.isNotBlank()) {
                        dataManager.saveSearchHistory(query)
                    }
                }
            )
        }

        binding.btnFilter.setOnClickListener {
            val bottomSheet = FilterBottomSheetFragment.newInstance(
                currentSort, currentCategory, minPrice, maxPrice
            )
            bottomSheet.setOnApplyListener { sort, cat, min, max ->
                currentSort = sort
                currentCategory = cat
                minPrice = min
                maxPrice = max

                val isFilterActive = (currentCategory != "Semua") || (minPrice > 0) || (maxPrice < Double.MAX_VALUE)

                if (currentQuery.isNotEmpty()) {
                    showResultView()
                } else if (isFilterActive) {
                    showResultView()
                } else {
                    showIdleView()
                }

                val listData = viewModel.allProduk.value
                if (listData != null) applyFilters(listData) else viewModel.getAllProduk()
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
            showResultView()
            applyFilters()
        } else {
            val isFilterActive = (currentCategory != "Semua") || (minPrice > 0) || (maxPrice < Double.MAX_VALUE)
            if (isFilterActive) {
                showResultView()
                applyFilters()
            } else {
                showIdleView()
            }
        }
    }

    private fun showResultView() {
        binding.layoutIdleState.visibility = View.GONE
        binding.recyclerSearchResults.visibility = View.VISIBLE
    }

    private fun showIdleView() {
        binding.layoutIdleState.visibility = View.VISIBLE
        binding.recyclerSearchResults.visibility = View.GONE
        loadIdleData()
    }

    private fun setupAdapters() {
        // 1. Search Results (Grid) -> FIX Parameter
        searchResultAdapter = ProductAdapter(
            products = emptyList(),
            onItemClick = { productId ->
                openDetail(productId)
            }
        )
        binding.recyclerSearchResults.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerSearchResults.adapter = searchResultAdapter

        // 2. Recent Search
        recentSearchAdapter = RecentSearchAdapter(
            mutableListOf(),
            onClick = { query ->
                handleSearchInput(query)
                Toast.makeText(context, "Mencari: $query", Toast.LENGTH_SHORT).show()
            },
            onRemove = { query ->
                dataManager.removeSearchItem(query)
                checkHistoryEmpty()
            }
        )
        binding.rvRecentSearch.layoutManager = LinearLayoutManager(context)
        binding.rvRecentSearch.adapter = recentSearchAdapter

        // 3. Recently Viewed
        recentlyViewedAdapter = RecentlyViewedAdapter(emptyList()) { productId ->
            openDetail(productId)
        }
        binding.rvRecentlyViewed.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentlyViewed.adapter = recentlyViewedAdapter
    }

    // Fungsi Helper Add to Cart
    private fun addToCart(produk: Produk) {
        if (produk.stok <= 0) {
            Toast.makeText(context, "Stok habis!", Toast.LENGTH_SHORT).show()
            return
        }
        val item = ItemPesanan(
            id = "",
            produkId = produk.id,
            produkNama = produk.nama,
            produkHarga = produk.harga,
            gambarUrl = produk.gambarUrl,
            jumlah = 1,
            isSelected = true
        )
        cartViewModel.insertItem(item)
        Toast.makeText(context, "Berhasil masuk keranjang!", Toast.LENGTH_SHORT).show()
    }

    // ... (sisa fungsi loadIdleData, updateRecentlyViewedList, applyFilters, openDetail, onDestroyView sama seperti sebelumnya) ...

    private fun loadIdleData() {
        val history = dataManager.getSearchHistory()
        recentSearchAdapter.updateData(history)
        checkHistoryEmpty()
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
                produk.nama.lowercase().contains(lowerCaseQuery) ||
                        produk.deskripsi.lowercase().contains(lowerCaseQuery)
            }
            val matchesPrice = produk.harga in minPrice..maxPrice
            val matchesCategory = if (currentCategory == "Semua") true else {
                produk.kategori.equals(currentCategory, ignoreCase = true)
            }
            matchesQuery && matchesPrice && matchesCategory
        }

        filteredList = when (currentSort) {
            "TERLARIS" -> filteredList.sortedByDescending { it.terjual }
            "HARGA_MURAH" -> filteredList.sortedBy { it.harga }
            "HARGA_MAHAL" -> filteredList.sortedByDescending { it.harga }
            else -> filteredList
        }
        searchResultAdapter.updateProducts(filteredList)
    }

    private fun openDetail(productId: String) {
        dataManager.saveRecentlyViewed(productId)
        val bundle = Bundle().apply { putString("productId", productId) }
        findNavController().navigate(R.id.action_global_to_detailProdukFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}