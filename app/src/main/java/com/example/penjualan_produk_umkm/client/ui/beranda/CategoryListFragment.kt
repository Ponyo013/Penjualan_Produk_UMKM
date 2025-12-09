package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.example.penjualan_produk_umkm.uiComponent.SearchBar
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.content.ContextCompat // Untuk ambil warna tema
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs
import androidx.core.view.updateLayoutParams
import android.graphics.Color
import kotlin.math.abs

private const val ARG_PARENT_CATEGORY = "parentCategoryName"

class CategoryListFragment : Fragment(R.layout.fragment_category_list) {

    private var parentCategoryName: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter

    // UI Components Baru
    private lateinit var ivBanner: ImageView
    // REMOVED: private lateinit var tvTitle: TextView
    // REMOVED: private lateinit var tvCount: TextView

    // State Filter & Sort & Search
    private var currentSort: ProductSortOption = ProductSortOption.TERBARU
    private var isReadyStockFilter: Boolean = false
    private var searchQuery: String = "" // Untuk pencarian lokal

    // Data Master (Semua produk dalam kategori ini)
    private var baseCategoryProducts: List<Produk> = emptyList()

    private val viewModel: ProdukViewModel by viewModels { ViewModelFactory() }
    private val cartViewModel: CartViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            parentCategoryName = it.getString(ARG_PARENT_CATEGORY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- FIX: DEFINISIKAN VARIABLE INI DULU ---
        val categoryTitle = parentCategoryName ?: "Semua Produk"
        // ------------------------------------------

        recyclerView = view.findViewById(R.id.popular_products_in_category)
        ivBanner = view.findViewById(R.id.iv_category_banner)
        // REMOVED: tvTitle = view.findViewById(R.id.tv_category_title)
        // REMOVED: tvCount = view.findViewById(R.id.tv_product_count)
        val appBarLayout = view.findViewById<AppBarLayout>(R.id.app_bar)
        // REMOVED: val filterSortContainer = view.findViewById<ComposeView>(R.id.filter_sort_container)
        val searchBarContainer = view.findViewById<ComposeView>(R.id.compose_search_bar_category)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar_category)

        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // REMOVED: tvTitle.text = categoryTitle
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Tambahkan margin top pada toolbar sebesar tinggi status bar
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }

        appBarLayout.addOnOffsetChangedListener { appBar, verticalOffset ->
            // Hitung apakah toolbar sedang 'collapsed' (tertutup penuh/putih)
            val scrollRange = appBar.totalScrollRange
            val isCollapsed = abs(verticalOffset) == scrollRange

            if (isCollapsed) {
                // Saat Toolbar Putih (Collapsed) -> Ubah Icon jadi Warna Tema (Biru Gelap)
                toolbar.setNavigationIconTint(ContextCompat.getColor(requireContext(), R.color.Secondary_1))
            } else {
                // Saat Banner Terlihat (Expanded) -> Ubah Icon jadi Putih agar kontras
                toolbar.setNavigationIconTint(Color.WHITE)
            }
        }
        setupBanner(categoryTitle)

        // Setup RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        productAdapter = ProductAdapter(
            products = emptyList(),
            onItemClick = { productId ->
                val bundle = Bundle().apply { putString("productId", productId) }
                findNavController().navigate(R.id.action_global_to_detailProdukFragment, bundle)
            }
        )
        recyclerView.adapter = productAdapter

        // --- SETUP SEARCH BAR (COMPOSE) ---
        searchBarContainer.setContent {
            UMKMTheme {
                SearchBar(
                    readOnly = false,
                    autoFocus = false,
                    onQueryChange = { query ->
                        searchQuery = query
                        applyFiltersAndSort() // Filter ulang saat mengetik
                    },
                    onSearchClicked = { /* Optional: Hide keyboard */ }
                )
            }
        }

        viewModel.allProduk.observe(viewLifecycleOwner) { produkList ->
            // Filter awal berdasarkan kategori dari navigasi
            val filteredList = if (parentCategoryName != null) {
                produkList.filter { it.kategori.equals(parentCategoryName, ignoreCase = true) }
            } else {
                produkList
            }
            baseCategoryProducts = filteredList

            // Terapkan filter lanjutan (search, sort, stock)
            applyFiltersAndSort()

            // REMOVED: filterSortContainer logic
        }

        // Load data jika kosong
        if (viewModel.allProduk.value.isNullOrEmpty()) {
            viewModel.getAllProduk()
        }
    }

    private fun setupBanner(categoryName: String) {
        val imageRes = when (categoryName.lowercase()) {
            "sepeda" -> R.drawable.banner_sepeda
            "spare parts", "sparepart" -> R.drawable.banner_spareparts
            "aksesoris" -> R.drawable.banner_aksesoris
            "perawatan" -> R.drawable.banner_perawatan
            else -> R.color.grey // Default/Placeholder
        }

        try {
            ivBanner.setImageResource(imageRes)
        } catch (e: Exception) {
            ivBanner.setImageResource(R.color.grey)
        }
    }

    private fun applyFiltersAndSort() {
        var resultList = baseCategoryProducts

        // 1. Filter Search Query
        if (searchQuery.isNotEmpty()) {
            resultList = resultList.filter {
                it.nama.contains(searchQuery, ignoreCase = true)
            }
        }

        // 2. Filter Stok
        if (isReadyStockFilter) {
            resultList = resultList.filter { it.stok > 0 }
        }

        // 3. Sorting
        resultList = when (currentSort) {
            ProductSortOption.TERBARU -> resultList // Default Firestore order
            ProductSortOption.TERLARIS -> resultList.sortedByDescending { it.terjual }
            ProductSortOption.HARGA_MAHAL -> resultList.sortedByDescending { it.harga }
            ProductSortOption.HARGA_MURAH -> resultList.sortedBy { it.harga }
        }

        // 4. Update Adapter
        productAdapter.updateProducts(resultList)
        // REMOVED: tvCount.text = "Menampilkan ${resultList.size} produk"
    }

    // Fungsi Helper Add to Cart (Jika ingin digunakan nanti)
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