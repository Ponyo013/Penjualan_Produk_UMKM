// File: com/example/penjualan_produk_umkm/client/ui/beranda/CategoryListFragment.kt

package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.produkDummyList
import com.example.penjualan_produk_umkm.model.Produk
import com.example.penjualan_produk_umkm.uiComponent.ProductFilterControls
import com.example.penjualan_produk_umkm.uiComponent.ProductSortOption
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.google.android.material.appbar.MaterialToolbar

private const val ARG_PARENT_CATEGORY = "parentCategoryName"

class CategoryListFragment : Fragment(R.layout.fragment_category_list) {

    private var parentCategoryName: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter

    // State Filter & Sort
    private var currentSort: ProductSortOption = ProductSortOption.TERBARU
    private var isReadyStockFilter: Boolean = false

    // List produk lengkap yang sudah difilter berdasarkan kategori
    private var baseCategoryProducts: List<Produk> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            parentCategoryName = it.getString(ARG_PARENT_CATEGORY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val categoryTitle = parentCategoryName ?: "Semua Produk"
        recyclerView = view.findViewById(R.id.popular_products_in_category)
        val filterSortContainer = view.findViewById<ComposeView>(R.id.filter_sort_container)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar_category)
        toolbar.title = categoryTitle
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        // 1. Inisialisasi Produk Kategori Dasar (Hanya sekali)
        baseCategoryProducts = produkDummyList.filter {
            it.kategori.equals(categoryTitle, ignoreCase = true)
        }


        // 2. Setup RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        productAdapter = ProductAdapter(emptyList()) { productId ->
            val bundle = Bundle().apply { putInt("productId", productId) }
            findNavController().navigate(R.id.action_global_to_detailProdukFragment, bundle)
        }
        recyclerView.adapter = productAdapter

        // 3. Setup Compose Filter/Sort Controls
        filterSortContainer.setContent {
            UMKMTheme {
                ProductFilterControls(
                    totalItemsCount = baseCategoryProducts.size,
                    currentSort = currentSort,
                    onSortChange = { newSort ->
                        currentSort = newSort
                        applyFiltersAndSort() // Terapkan Sort baru
                    },
                    onStockFilterChange = { isChecked ->
                        isReadyStockFilter = isChecked
                        applyFiltersAndSort() // Terapkan Filter baru
                    }
                )
            }
        }

        // 4. Tampilkan produk awal
        applyFiltersAndSort()
    }

    /**
     * Fungsi utama untuk menerapkan filter dan sorting pada daftar produk.
     */
    private fun applyFiltersAndSort() {
        var resultList = baseCategoryProducts

        // A. FILTER READY STOCK
        if (isReadyStockFilter) {
            resultList = resultList.filter { it.stok > 0 }
        }

        // B. SORTING
        resultList = when (currentSort) {
            ProductSortOption.TERBARU -> resultList.sortedByDescending { it.id } // Asumsi ID yang lebih tinggi = lebih baru
            ProductSortOption.TERLARIS -> resultList.sortedByDescending { it.terjual }
            ProductSortOption.HARGA_MAHAL -> resultList.sortedByDescending { it.harga }
            ProductSortOption.HARGA_MURAH -> resultList.sortedBy { it.harga }
        }

        // C. Update UI
        productAdapter.updateProducts(resultList)
    }

    companion object {
        @JvmStatic
        fun newInstance(parentCategoryName: String) =
            CategoryListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARENT_CATEGORY, parentCategoryName)
                }
            }
    }
}