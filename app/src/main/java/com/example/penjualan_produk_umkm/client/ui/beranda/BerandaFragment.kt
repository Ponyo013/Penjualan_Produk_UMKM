package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.database.model.Artikel
import com.example.penjualan_produk_umkm.uiComponent.SearchBar
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel

class BerandaFragment : Fragment(R.layout.fragment_beranda) {

    private lateinit var rvBestSeller: RecyclerView
    private lateinit var bestSellerAdapter: ProductAdapter

    private lateinit var rvRecommendation: RecyclerView
    private lateinit var recommendationAdapter: RecommendationAdapter

    private lateinit var rvArticles: RecyclerView

    // 1. Tambahkan ProdukViewModel (untuk data produk)
    private val viewModel: ProdukViewModel by viewModels {
        ViewModelFactory()
    }

    // 2. Tambahkan CartViewModel (untuk fungsi Add to Cart)
    private val cartViewModel: CartViewModel by viewModels {
        ViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        // --- 1. SETUP ARTIKEL (NEW) ---
        rvArticles = view.findViewById(R.id.rv_articles)
        setupArticleSection()

        // --- Setup RecyclerView REKOMENDASI (Horizontal) ---
        rvRecommendation = view.findViewById(R.id.rv_recommendation)
        rvRecommendation.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recommendationAdapter = RecommendationAdapter(emptyList()) { productId ->
            openDetail(productId)
        }
        rvRecommendation.adapter = recommendationAdapter

        // --- Setup RecyclerView TERLARIS (Grid) ---
        rvBestSeller = view.findViewById(R.id.recycler_popular_products)
        rvBestSeller.layoutManager = GridLayoutManager(context, 2)

        // FIX: Inisialisasi ProductAdapter dengan 3 parameter (List, Click Detail, Click Cart)
        bestSellerAdapter = ProductAdapter(
            products = emptyList(),
            onItemClick = { productId ->
                openDetail(productId)
            },
        )
        rvBestSeller.adapter = bestSellerAdapter

        // --- Setup Search Bar (Compose) ---
        val composeView = view.findViewById<ComposeView>(R.id.compose_search_bar)
        composeView.setContent {
            SearchBar(
                readOnly = true, // AKTIFKAN MODE TOMBOL
                onClick = {
                    // Langsung pindah ke Search Fragment saat disentuh
                    findNavController().navigate(R.id.action_global_to_searchFragment)
                }
            )
        }

        return view
    }
    private fun setupArticleSection() {
        // Data Dummy Artikel (Bisa diganti data dinamis dari Firebase nanti)
        val articles = listOf(
            Artikel(1, "Tips Merawat Gear Sepeda Agar Awet", "https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?q=80&w=2000&auto=format&fit=crop", "https://www.rodalink.com/id/blog"),
            Artikel(2, "Rute Gowes Terbaik di Jakarta", "https://images.unsplash.com/photo-1541625602330-2277a4c46182?q=80&w=1000&auto=format&fit=crop", "https://www.google.com"),
            Artikel(
                3,
                "Review: Polygon Siskiu D6",
                "https://ik.imagekit.io/ngj1vwwr8/produk/siskiud6.webp",
                "https://www.polygonbikes.com"
            )
        )

        val adapter = ArtikelAdapter(articles)

        // Layout Manager Grid 2 Kolom
        val layoutManager = GridLayoutManager(context, 2)

        // LOGIKA SPAN (PENTING!): Item pertama lebar penuh, sisanya setengah
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) 2 else 1
            }
        }

        rvArticles.layoutManager = layoutManager
        rvArticles.adapter = adapter

        // Disable scrolling recyclerView artikel agar smooth dengan ScrollView induk
        rvArticles.isNestedScrollingEnabled = false
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigasi Icon Header
        view.findViewById<View>(R.id.notification_icon).setOnClickListener {
            findNavController().navigate(R.id.action_berandaFragment_to_notificationFragment)
        }
        view.findViewById<View>(R.id.cart_icon).setOnClickListener {
            findNavController().navigate(R.id.action_BerandaFragment_to_CartFragment)
        }

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.allProduk.observe(viewLifecycleOwner) { produkList ->

            // --- FILTER LOGIC ---

            // 1. Rekomendasi: Rating > 4.5 DAN Terjual >= 100
            val recommendations = produkList.filter {
                it.rating > 4.5f && it.terjual >= 100
            }.sortedByDescending { it.rating }

            // 2. Produk Terlaris: Urutkan berdasarkan penjualan tertinggi
            val bestSellers = produkList.sortedByDescending { it.terjual }

            // Update ke Adapter
            recommendationAdapter.updateData(recommendations)
            bestSellerAdapter.updateProducts(bestSellers)
        }
    }

    private fun openDetail(productId: String) {
        val bundle = Bundle().apply { putString("productId", productId) }
        findNavController().navigate(R.id.action_BerandaFragment_to_detailProdukFragment, bundle)
    }

    // --- FUNGSI ADD TO CART ---
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
}