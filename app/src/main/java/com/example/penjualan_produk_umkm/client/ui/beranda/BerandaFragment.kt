package com.example.penjualan_produk_umkm.client.ui.beranda

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.database.model.Artikel
import com.example.penjualan_produk_umkm.uiComponent.SearchBar
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class BerandaFragment : Fragment(R.layout.fragment_beranda) {

    private lateinit var rvBestSeller: RecyclerView
    private lateinit var bestSellerAdapter: ProductAdapter
    private lateinit var rvRecommendation: RecyclerView
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var rvArticles: RecyclerView
    private lateinit var header: ConstraintLayout
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var notificationIcon: FrameLayout
    private lateinit var cartIcon: FrameLayout
    private lateinit var statusBarBackground: View

    private val viewModel: ProdukViewModel by viewModels {
        ViewModelFactory()
    }

    private val cartViewModel: CartViewModel by viewModels {
        ViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        header = view.findViewById(R.id.header)
        nestedScrollView = view.findViewById(R.id.nested_scroll_view)
        notificationIcon = view.findViewById(R.id.notification_icon)
        cartIcon = view.findViewById(R.id.cart_icon)
        statusBarBackground = view.findViewById(R.id.status_bar_background)

        rvArticles = view.findViewById(R.id.rv_articles)
        setupArticleSection()

        rvRecommendation = view.findViewById(R.id.rv_recommendation)
        rvRecommendation.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recommendationAdapter = RecommendationAdapter(emptyList()) { productId ->
            openDetail(productId)
        }
        rvRecommendation.adapter = recommendationAdapter

        rvBestSeller = view.findViewById(R.id.recycler_popular_products)
        rvBestSeller.layoutManager = GridLayoutManager(context, 2)

        bestSellerAdapter = ProductAdapter(
            products = emptyList(),
            onItemClick = { productId ->
                openDetail(productId)
            },
        )
        rvBestSeller.adapter = bestSellerAdapter

        val composeView = view.findViewById<ComposeView>(R.id.compose_search_bar)
        composeView.setContent {
            SearchBar(
                readOnly = true,
                onClick = {
                    findNavController().navigate(R.id.action_global_to_searchFragment)
                }
            )
        }

        return view
    }

    private fun setupArticleSection() {
        val articles = listOf(
            Artikel(1, "Tips Merawat Gear Sepeda Agar Awet", "https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?q=80&w=2000&auto=format&fit=crop", "https://www.rodalink.com/id/blog"),
            Artikel(2, "Rute Gowes Terbaik di Jakarta", "https://images.unsplash.com/photo-1541625602330-2277a4c46182?q=80&w=1000&auto=format&fit=crop", "https://www.google.com"),
            Artikel(3, "Review: Polygon Siskiu D6", "https://ik.imagekit.io/ngj1vwwr8/produk/siskiud6.webp", "https://www.polygonbikes.com")
        )

        val adapter = ArtikelAdapter(articles)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvArticles.layoutManager = layoutManager
        rvArticles.adapter = adapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvArticles)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarBackground.updateLayoutParams { height = systemBars.top }
            insets
        }

        notificationIcon.setOnClickListener {
            findNavController().navigate(R.id.action_berandaFragment_to_notificationFragment)
        }
        cartIcon.setOnClickListener {
            findNavController().navigate(R.id.action_BerandaFragment_to_CartFragment)
        }

        setupObservers()
        setupHeaderScroll()
    }

    private fun setupHeaderScroll() {
        nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val shouldShowWhiteBackground = scrollY > rvArticles.height
            if (shouldShowWhiteBackground) {
                header.setBackgroundColor(Color.WHITE)
                notificationIcon.setBackgroundResource(R.drawable.circle_frame)
                cartIcon.setBackgroundResource(R.drawable.circle_frame)
            } else {
                header.setBackgroundColor(Color.TRANSPARENT)
                notificationIcon.setBackgroundResource(R.drawable.circle_frame_transparent)
                cartIcon.setBackgroundResource(R.drawable.circle_frame_transparent)
            }
        }
    }

    private fun setupObservers() {
        viewModel.allProduk.observe(viewLifecycleOwner) { produkList ->
            val recommendations = produkList.filter {
                it.rating > 4.5f && it.terjual >= 100 && it.stok > 0
            }.sortedByDescending { it.rating }

            val bestSellers = produkList
                .filter { it.stok > 0 }
                .sortedByDescending { it.terjual }

            recommendationAdapter.updateData(recommendations)
            bestSellerAdapter.updateProducts(bestSellers)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                cartViewModel.totalQuantity.collectLatest { totalQty ->
                    val badge = view?.findViewById<android.widget.TextView>(R.id.tv_cart_badge)

                    if (totalQty > 0) {
                        badge?.visibility = View.VISIBLE
                        // Jika lebih dari 99, tampilkan "99+" agar rapi
                        badge?.text = if (totalQty > 99) "99+" else totalQty.toString()
                    } else {
                        badge?.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun openDetail(productId: String) {
        val bundle = Bundle().apply { putString("productId", productId) }
        findNavController().navigate(R.id.action_BerandaFragment_to_detailProdukFragment, bundle)
    }

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