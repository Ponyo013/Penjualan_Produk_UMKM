package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.style.UMKMTheme
import com.example.penjualan_produk_umkm.uiComponent.ProductFilterControls
import com.example.penjualan_produk_umkm.uiComponent.ProductSortOption
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
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
    private var baseCategoryProducts: List<Produk> = emptyList()

    private val viewModel: ProdukViewModel by viewModels { ViewModelFactory() }

    // TAMBAHAN: CartViewModel untuk fitur add to cart
    private val cartViewModel: CartViewModel by viewModels { ViewModelFactory() }

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

        // FIX: Inisialisasi Adapter dengan 3 Parameter
        productAdapter = ProductAdapter(
            products = emptyList(),
            onItemClick = { productId ->
                val bundle = Bundle().apply { putString("productId", productId) }
                findNavController().navigate(R.id.action_global_to_detailProdukFragment, bundle)
            },

        )
        recyclerView.adapter = productAdapter

        viewModel.allProduk.observe(viewLifecycleOwner) { produkList ->
            val filteredList = if (parentCategoryName != null) {
                produkList.filter { it.kategori.equals(parentCategoryName, ignoreCase = true) }
            } else {
                produkList
            }
            baseCategoryProducts = filteredList
            applyFiltersAndSort()

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

    private fun applyFiltersAndSort() {
        var resultList = baseCategoryProducts
        if (isReadyStockFilter) {
            resultList = resultList.filter { it.stok > 0 }
        }
        resultList = when (currentSort) {
            ProductSortOption.TERBARU -> resultList
            ProductSortOption.TERLARIS -> resultList.sortedByDescending { it.terjual }
            ProductSortOption.HARGA_MAHAL -> resultList.sortedByDescending { it.harga }
            ProductSortOption.HARGA_MURAH -> resultList.sortedBy { it.harga }
        }
        productAdapter.updateProducts(resultList)
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
        Toast.makeText(context, "Masuk Keranjang", Toast.LENGTH_SHORT).show()
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