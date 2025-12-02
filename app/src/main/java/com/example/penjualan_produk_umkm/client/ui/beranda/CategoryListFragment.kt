// File: com/example/penjualan_produk_umkm/client/ui/beranda/CategoryListFragment.kt

package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.firestore.model.Produk // Pastikan Import Model Firestore
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.uiComponent.ProductFilterControls
import com.example.penjualan_produk_umkm.uiComponent.ProductSortOption
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
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

    // FIX 1: Hapus AppDatabase, gunakan ViewModelFactory kosong
    private val viewModel: ProdukViewModel by viewModels {
        ViewModelFactory()
    }

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
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Setup RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        productAdapter = ProductAdapter(emptyList()) { productId ->
            // FIX 2: ID Produk sekarang String
            val bundle = Bundle().apply { putString("productId", productId) }
            findNavController().navigate(R.id.action_global_to_detailProdukFragment, bundle)
        }
        recyclerView.adapter = productAdapter

        // FIX 3: Amati data produk dari ViewModel (Firestore)
        // Tidak perlu panggil viewModel.getAllProduk() manual jika di blok init ViewModel sudah dipanggil
        // Tapi untuk memastikan data terupdate jika belum:
        // viewModel.getAllProduk() // Opsional, tergantung implementasi VM Anda

        viewModel.allProduk.observe(viewLifecycleOwner) { produkList ->

            // --- FILTER KATEGORI ---
            val filteredList = if (parentCategoryName != null) {
                produkList.filter { it.kategori.equals(parentCategoryName, ignoreCase = true) }
            } else {
                produkList
            }

            baseCategoryProducts = filteredList
            applyFiltersAndSort()

            // --- UPDATE COMPOSE UI ---
            filterSortContainer.setContent {
                UMKMTheme {
                    ProductFilterControls(
                        totalItemsCount = baseCategoryProducts.size,
                        currentSort = currentSort,
                        onSortChange = { newSort ->
                            currentSort = newSort
                            applyFiltersAndSort()
                        },
                        onStockFilterChange = { isChecked ->
                            isReadyStockFilter = isChecked
                            applyFiltersAndSort()
                        }
                    )
                }
            }
        }
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
            // FIX 4: Firestore ID adalah String, sortedByDescending id mungkin kurang efektif untuk "Terbaru"
            // jika ID-nya random string. Idealnya ada field 'tanggalUpload' di Firestore.
            // Tapi untuk sekarang biarkan sortedByDescending { it.id } atau { it.nama }
            ProductSortOption.TERBARU -> resultList // Sementara tampilkan apa adanya atau sort by name
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