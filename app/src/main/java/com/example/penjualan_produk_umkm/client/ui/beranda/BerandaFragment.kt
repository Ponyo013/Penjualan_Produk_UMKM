package com.example.penjualan_produk_umkm.client.ui.beranda

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.database.model.Artikel
import com.example.penjualan_produk_umkm.uiComponent.SearchBar
import com.example.penjualan_produk_umkm.viewModel.CartViewModel
import com.example.penjualan_produk_umkm.viewModel.NotificationViewModel
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import com.example.penjualan_produk_umkm.database.model.Koleksi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BerandaFragment : Fragment(R.layout.fragment_beranda) {
    // --- UI Components ---
    private lateinit var header: ConstraintLayout
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var notificationIcon: FrameLayout
    private lateinit var cartIcon: FrameLayout
    private lateinit var statusBarBackground: View

    // --- Adapters & RecyclerViews ---
    private lateinit var rvBestSeller: RecyclerView
    private lateinit var bestSellerAdapter: ProductAdapter
    private lateinit var rvRecommendation: RecyclerView
    private lateinit var recommendationAdapter: RecommendationAdapter

    // --- ViewPagers (Carousel) ---
    private lateinit var vpArticles: ViewPager2
    private lateinit var rvCollections: RecyclerView

    // HAPUS collectionHandler & collectionRunnable

    // --- Auto Slide Handlers ---
    private val sliderHandler = Handler(Looper.getMainLooper())
    private lateinit var sliderRunnable: Runnable


    // --- ViewModels ---
    private val viewModel: ProdukViewModel by viewModels { ViewModelFactory() }
    private val cartViewModel: CartViewModel by viewModels { ViewModelFactory() }
    private val notificationViewModel: NotificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        // Inisialisasi View
        header = view.findViewById(R.id.header)
        nestedScrollView = view.findViewById(R.id.nested_scroll_view)
        notificationIcon = view.findViewById(R.id.notification_icon)
        cartIcon = view.findViewById(R.id.cart_icon)
        statusBarBackground = view.findViewById(R.id.status_bar_background)

        // Inisialisasi ViewPager Artikel
        vpArticles = view.findViewById(R.id.vp_articles)
        setupArticleSection()

        // Inisialisasi ViewPager Koleksi
        rvCollections = view.findViewById(R.id.rv_collections) // Pastikan ID XML sudah diganti
        setupCollectionSection()

        // Inisialisasi Rekomendasi
        rvRecommendation = view.findViewById(R.id.rv_recommendation)
        rvRecommendation.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recommendationAdapter = RecommendationAdapter(emptyList()) { productId ->
            openDetail(productId)
        }
        rvRecommendation.adapter = recommendationAdapter

        // Inisialisasi Best Seller
        rvBestSeller = view.findViewById(R.id.recycler_popular_products)
        rvBestSeller.layoutManager = GridLayoutManager(context, 2)
        bestSellerAdapter = ProductAdapter(
            products = emptyList(),
            onItemClick = { productId ->
                openDetail(productId)
            },
        )
        rvBestSeller.adapter = bestSellerAdapter

        // Inisialisasi Search Bar Compose
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
    private fun setupArticleSection() {
        val articles = listOf(
            Artikel(1, "Tips Merawat Gear Sepeda Agar Awet", "https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?q=80&w=2000&auto=format&fit=crop", "https://www.rodalink.com/id/blog"),
            Artikel(2, "Aman Bersepeda Listrik Saat Hujan?", "https://ik.imagekit.io/ngj1vwwr8/produk/id-bisakah_menggunakan_sepeda_listrik_saat_hujan-header.jpg", "https://www.rodalink.com/id/blog"),
            Artikel(3, "Review: Polygon Siskiu D6", "https://ik.imagekit.io/ngj1vwwr8/produk/siskiud6.webp", "https://www.polygonbikes.com")
        )

        // Setup Adapter ViewPager
        val adapter = ArtikelAdapter(articles)
        vpArticles.adapter = adapter

        // Auto Slide Logic for Articles
        sliderRunnable = Runnable {
            val itemCount = vpArticles.adapter?.itemCount ?: 0
            if (itemCount > 0) {
                // Pindah ke item berikutnya. Jika sudah di akhir, balik ke 0.
                val nextItem = (vpArticles.currentItem + 1) % itemCount
                vpArticles.setCurrentItem(nextItem, true)
                sliderHandler.postDelayed(sliderRunnable, 4000)
            }
        }
        // Mulai Slide
        sliderHandler.postDelayed(sliderRunnable, 4000)

        vpArticles.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 4000)
            }
        })
    }

    private fun setupCollectionSection() {
        val collections = listOf(
            Koleksi(1, "SEPEDA", "", R.drawable.c_sepeda, "Sepeda"),
            Koleksi(2, "SPARE PARTS", "", R.drawable.c_spareparts, "Spare Parts"),
            Koleksi(3, "AKSESORIS", "", R.drawable.c_aksesoris, "Aksesoris"),
            Koleksi(4, "PERAWATAN", "", R.drawable.c_perawatan, "Perawatan")
        )

        val adapter = CollectionAdapter(collections) { category ->
            openCategoryList(category)
        }

        // --- KONFIGURASI SCROLL HORIZONTAL (SEPERTI GAMBAR 2) ---
        // Gunakan LinearLayoutManager Horizontal
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvCollections.layoutManager = layoutManager
        rvCollections.adapter = adapter

        // Aktifkan scroll (hapus isNestedScrollingEnabled = false jika sebelumnya ada)
        rvCollections.isNestedScrollingEnabled = true
        // --------------------------------------------------------
    }
    private fun setupHeaderScroll() {
        nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            // Threshold based on Article Banner height
            val threshold = vpArticles.height - 100
            // Logika Transisi Warna
            if (scrollY > threshold) {
                // KONDISI: SUDAH DI-SCROLL KE BAWAH (Mode Putih)
                header.setBackgroundColor(Color.WHITE)
                statusBarBackground.setBackgroundColor(Color.WHITE)
                notificationIcon.setBackgroundResource(R.drawable.circle_frame)
                cartIcon.setBackgroundResource(R.drawable.circle_frame)

            } else {
                header.setBackgroundColor(Color.TRANSPARENT)
                statusBarBackground.setBackgroundColor(Color.TRANSPARENT)
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

            recommendationAdapter.updateData(recommendations)
            bestSellerAdapter.updateProducts(bestSellers)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    cartViewModel.totalQuantity.collectLatest { totalQty ->
                        val badge = view?.findViewById<TextView>(R.id.tv_cart_badge)

                        if (badge != null) {
                            if (totalQty > 0) {
                                badge.visibility = View.VISIBLE
                                badge.text = if (totalQty > 99) "99+" else totalQty.toString()
                            } else {
                                badge.visibility = View.GONE
                            }
                        }
                    }
                }
                launch {
                    notificationViewModel.unreadNotificationCount.collectLatest { count ->
                        val badge = view?.findViewById<TextView>(R.id.tv_notification_badge)
                        if (badge != null) {
                            if (count > 0) {
                                badge.visibility = View.VISIBLE
                                badge.text = if (count > 99) "99+" else count.toString()
                            } else {
                                badge.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    // --- NAVIGATION HELPERS ---

    private fun openDetail(productId: String) {
        val bundle = Bundle().apply { putString("productId", productId) }
        findNavController().navigate(R.id.action_BerandaFragment_to_detailProdukFragment, bundle)
    }

    private fun openCategoryList(categoryName: String) {
        // Menggunakan bundle manual karena safeargs mungkin belum di-generate
        val bundle = Bundle().apply {
            putString("parentCategoryName", categoryName)
        }
        // Pastikan ID ini sesuai dengan ID fragment di nav_graph_client.xml
        // Jika belum ada action global, gunakan ID tujuannya langsung
        try {
            findNavController().navigate(R.id.action_global_to_categoryListFragment, bundle)
        } catch (e: Exception) {
            // Fallback jika action global tidak ditemukan, coba navigasi langsung ke ID fragment
            // Pastikan ID 'categoryListFragment' ada di nav graph Anda
            try {
                findNavController().navigate(R.id.categoryListFragment, bundle)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 4000)
    }

}