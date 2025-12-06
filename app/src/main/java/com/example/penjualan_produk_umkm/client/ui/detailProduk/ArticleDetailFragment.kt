package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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

        // --- 1. INISIALISASI VIEW ---
        val btnBack = view.findViewById<View>(R.id.btn_back_header)
        val ivHero = view.findViewById<ImageView>(R.id.iv_hero_image)

        // Pastikan ID ini sudah ditambahkan di XML
        val tvCategory = view.findViewById<TextView>(R.id.tv_article_category)
        val tvTitle = view.findViewById<TextView>(R.id.tv_article_title_detail)
        val tvDate = view.findViewById<TextView>(R.id.tv_article_date)
        val tvIntro = view.findViewById<TextView>(R.id.tv_article_intro)

        // Section Body
        val tvSection1Title = view.findViewById<TextView>(R.id.tv_section_1_title)
        val tvSection1Body = view.findViewById<TextView>(R.id.tv_section_1_body)
        val tvSection2Title = view.findViewById<TextView>(R.id.tv_section_2_title)
        val tvSection2Body = view.findViewById<TextView>(R.id.tv_section_2_body)

        // CardView Tips (Untuk di-hide saat artikel review)
        val cvTipsContainer = view.findViewById<LinearLayout>(R.id.layout_tips_container)
        val tvRelatedTitle = view.findViewById<TextView>(R.id.tv_related_title)
        val rvRelated = view.findViewById<RecyclerView>(R.id.rv_related_products)

        // --- LOAD GAMBAR ARTIKEL (DARI IMAGEKIT) ---

        // URL Gambar dari ImageKit Anda
        val imgPrerideCheck = "https://ik.imagekit.io/ngj1vwwr8/produk/preridecheck.jpg"
        val imgLube = "https://ik.imagekit.io/ngj1vwwr8/produk/pelumassepeda.jpg"

        // Load ke ImageView Step 1 (Harian - Cek Ban/Rem)
        view.findViewById<ImageView>(R.id.iv_step_1).load(imgPrerideCheck) {
            crossfade(true)
            placeholder(R.color.grey) // Warna abu-abu saat loading
            error(R.drawable.ic_error_image) // Gambar error jika gagal load
        }

        // Load ke ImageView Step 2 (Mingguan - Pelumas)
        view.findViewById<ImageView>(R.id.iv_step_2).load(imgLube) {
            crossfade(true)
            placeholder(R.color.grey)
            error(R.drawable.ic_error_image)
        }
        // --- 2. SETUP RECYCLER VIEW ---
        rvRelated.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        relatedProductAdapter = RecommendationAdapter(emptyList()) { productId ->
            val bundle = Bundle().apply { putString("productId", productId) }
            findNavController().navigate(R.id.action_global_to_detailProdukFragment, bundle)
        }
        rvRelated.adapter = relatedProductAdapter

        // --- 3. AMBIL DATA ARGUMEN ---
        val contentId = arguments?.getInt("ARG_ID") ?: 1
        val argImageUrl = arguments?.getString("ARG_IMAGE")
        val argTitle = arguments?.getString("ARG_TITLE")

        // Load Hero Image (Prioritas dari Argument)
        if (!argImageUrl.isNullOrEmpty()) {
            ivHero.load(argImageUrl) {
                crossfade(true)
                placeholder(R.color.grey)
            }
        }

        // --- 4. LOGIKA KONTEN DINAMIS ---
        if (contentId == 3) {
            // === ARTIKEL REVIEW SISKIU D6 ===

            tvCategory?.text = "Review Produk"
            tvTitle.text = getString(R.string.article_title_siskiu)
            tvDate?.text = getString(R.string.article_date_siskiu)
            tvIntro?.text = getString(R.string.article_intro_siskiu)

            tvSection1Title?.text = "Bedah Fitur & Performa"
            // Gunakan Html.fromHtml jika string resource mengandung tag <b>
            tvSection1Body?.text = "${getString(R.string.article_body_siskiu_1)}\n\n${getString(R.string.article_body_siskiu_2)}\n\n${getString(R.string.article_body_siskiu_3)}"

            tvSection2Title?.text = "Kesimpulan"
            tvSection2Body?.text = getString(R.string.article_conclusion_siskiu)

            // Sembunyikan Layout Tips (Preride check dll)
            cvTipsContainer?.visibility = View.GONE

            // Ubah Judul Rekomendasi
            tvRelatedTitle?.text = "Beli Polygon Siskiu D6 Sekarang"

            // FILTER PRODUK: HANYA SISKIU D6
            viewModel.allProduk.observe(viewLifecycleOwner) { allProducts ->
                val reviewProduct = allProducts.filter {
                    it.nama.contains("Siskiu D6", ignoreCase = true)
                }
                relatedProductAdapter.updateData(reviewProduct)
            }

        } else {
            // === ARTIKEL DEFAULT (PERAWATAN) ===

            tvCategory?.text = "Tips & Trik"
            // Jika ada judul dari argumen (misal dikirim dari adapter), pakai itu. Jika tidak, pakai default XML.
            if (!argTitle.isNullOrEmpty()) tvTitle.text = argTitle

            // Kembalikan ke default XML strings untuk teks lainnya (tidak perlu di-set manual jika XML sudah benar)
            // Pastikan container tips terlihat
            cvTipsContainer?.visibility = View.VISIBLE
            tvRelatedTitle?.text = "Produk Perawatan Pilihan"

            // FILTER PRODUK: PERAWATAN
            viewModel.allProduk.observe(viewLifecycleOwner) { allProducts ->
                val maintenanceProducts = allProducts.filter {
                    val cat = it.kategori.lowercase()
                    cat.contains("perawatan") || cat.contains("tools")
                }.take(5)

                if (maintenanceProducts.isNotEmpty()) {
                    relatedProductAdapter.updateData(maintenanceProducts)
                } else {
                    // Fallback jika kategori perawatan kosong -> Tampilkan Aksesoris
                    val fallback = allProducts.filter { it.kategori.equals("Aksesoris", true) }.take(5)
                    relatedProductAdapter.updateData(fallback)
                }
            }
        }

        // Load data if empty
        if (viewModel.allProduk.value.isNullOrEmpty()) {
            viewModel.getAllProduk()
        }

        // Tombol Back
        btnBack.setOnClickListener { findNavController().popBackStack() }
    }
}