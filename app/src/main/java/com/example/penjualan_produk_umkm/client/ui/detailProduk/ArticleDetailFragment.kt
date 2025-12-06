package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.client.ui.beranda.RecommendationAdapter
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel

class ArticleDetailFragment : Fragment() {

    private val viewModel: ProdukViewModel by viewModels { ViewModelFactory() }
    private lateinit var relatedProductAdapter: RecommendationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_article_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<View>(R.id.btn_back_header)
        val ivHero = view.findViewById<ImageView>(R.id.iv_hero_image)
        val tvTitle = view.findViewById<TextView>(R.id.tv_article_title_detail)

        // Inisialisasi RecyclerView Related Products
        val rvRelated = view.findViewById<RecyclerView>(R.id.rv_related_products)
        rvRelated.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Gunakan RecommendationAdapter yang sudah ada (layout card kecil)
        relatedProductAdapter = RecommendationAdapter(emptyList()) { productId ->
            val bundle = Bundle().apply { putString("productId", productId) }
            findNavController().navigate(R.id.action_global_to_detailProdukFragment, bundle)
        }
        rvRelated.adapter = relatedProductAdapter

        // --- LOAD GAMBAR ARTIKEL ---
        // Anda bisa ganti URL ini nanti dengan URL asli di kode DataSeeder atau Firebase
        val dummyImage1 = "https://ik.imagekit.io/demo/img/image4.jpeg" // Contoh
        val dummyImage2 = "https://ik.imagekit.io/demo/img/image5.jpeg"

        view.findViewById<ImageView>(R.id.iv_step_1).load(dummyImage1) {
            crossfade(true)
            placeholder(R.color.grey)
        }
        view.findViewById<ImageView>(R.id.iv_step_2).load(dummyImage2) {
            crossfade(true)
            placeholder(R.color.grey)
        }

        // --- AMBIL DATA PRODUK UNTUK REKOMENDASI ---
        // Filter hanya produk kategori "Perawatan"
        viewModel.allProduk.observe(viewLifecycleOwner) { allProducts ->
            val maintenanceProducts = allProducts.filter {
                it.kategori.equals("Perawatan", ignoreCase = true) ||
                        it.kategori.equals("Aksesoris", ignoreCase = true) // Fallback jika perawatan sedikit
            }.take(5) // Ambil 5 saja

            relatedProductAdapter.updateData(maintenanceProducts)
        }

        // Load data jika belum ada
        if (viewModel.allProduk.value.isNullOrEmpty()) {
            viewModel.getAllProduk()
        }

        // ... (Sisa kode back button, set title, dll sama seperti sebelumnya) ...
        val title = arguments?.getString("ARG_TITLE")
        val imageUrl = arguments?.getString("ARG_IMAGE")

        if (!title.isNullOrEmpty()) tvTitle.text = title
        if (!imageUrl.isNullOrEmpty()) {
            ivHero.load(imageUrl) { crossfade(true); placeholder(R.color.grey) }
        }

        btnBack.setOnClickListener { findNavController().popBackStack() }
    }
}