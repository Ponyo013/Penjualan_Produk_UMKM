package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.produkDummyList
import com.example.penjualan_produk_umkm.model.Produk
import java.text.NumberFormat
import java.util.*
import coil.load
import android.widget.FrameLayout

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DetailProdukFragment : Fragment(R.layout.fragment_detail_produk) {
    private var param1: String? = null
    private var param2: String? = null
    // Pindahkan deklarasi produk di sini
    internal var produk: Produk? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_produk, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. AMBIL ID PRODUK DARI ARGUMEN (BUNDLE)
        val productId = arguments?.getInt("productId")

        // 2. CARI DATA PRODUK
        produk = produkDummyList.find { it.id == productId }

        // Jika produk ditemukan, isi UI
        produk?.let { p ->
            setupToolbar(view)
            setupProductInfo(view, p)
            setupRatingInfo(view, p)

            val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
            val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
            val adapter = DetailPagerAdapter(this)

            viewPager.adapter = adapter
            viewPager.isUserInputEnabled = false // **Matikan scrolling internal**

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Deskripsi"
                    1 -> "Spesifikasi"
                    2 -> "Ulasan"
                    else -> ""
                }
            }.attach()

            // 1. Tambahkan listener penyesuaian tinggi
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    adjustViewPagerHeight(viewPager, adapter)
                }
            })

            // 2. Terapkan tinggi inisial (Post memastikan View sudah ready)
            view.post {
                adjustViewPagerHeight(viewPager, adapter)
            }

            // Ketika area rating diklik, pindah ke Tab Ulasan (Indeks 2)
            val ratingContainer = view.findViewById<FrameLayout>(R.id.rating)
//            val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)

            ratingContainer.setOnClickListener {
                viewPager.setCurrentItem(2, true)
            }

        } ?: run {
            findNavController().popBackStack()
        }
    }

    // =========================================================================
    // FUNCTIONS HELPER (DIPINDAHKAN KE LUAR onViewCreated)
    // =========================================================================
    private fun measureFragmentHeight(fragment: Fragment): Int {
        if (fragment.view == null) return 0
        fragment.view?.measure(
            View.MeasureSpec.makeMeasureSpec(view?.width ?: 0, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return fragment.view?.measuredHeight ?: 0
    }

    private fun adjustViewPagerHeight(viewPager: ViewPager2, adapter: DetailPagerAdapter) {
        val position = viewPager.currentItem
        val fragmentTag = "f" + position
        val currentFragment = childFragmentManager.findFragmentByTag(fragmentTag)

        if (currentFragment?.view == null) {
            // Fragment belum di-attach abaikan.
            return
        }

        viewPager.postDelayed({
            currentFragment.view?.let { fragmentView ->
                fragmentView.measure(
                    View.MeasureSpec.makeMeasureSpec(viewPager.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED) // Biarkan tinggi tak terbatas
                )

                // Mendapatkan tinggi yang diukur
                val height = fragmentView.measuredHeight

                if (height > 0 && height != viewPager.height) {
                    viewPager.layoutParams = viewPager.layoutParams.apply {
                        // Tetapkan tinggi ViewPager sesuai tinggi konten anak + sedikit padding
                        this.height = height + 16 // Tambahkan 16dp margin aman
                    }
                }
            }
        }, 50)
    }
    private fun setupProductInfo(view: View, produk: Produk) {
        val bannerImage = view.findViewById<ImageView>(R.id.banner_image)
        val namaProduk = view.findViewById<TextView>(R.id.nama_produk)
        val hargaProduk = view.findViewById<TextView>(R.id.harga_produk)
        val stockStatus = view.findViewById<TextView>(R.id.stock_status) // <-- NEW ID
        val btnCart = view.findViewById<Button>(R.id.btn_add_to_cart)
        val galleryViewPager = view.findViewById<ViewPager2>(R.id.gallery_view_pager)
        galleryViewPager.adapter = GalleryAdapter(produk.gambarResourceIds)

        val firstImageId = produk.gambarResourceIds.firstOrNull()
        if (firstImageId != null) {
            bannerImage.load(firstImageId) { // <-- Load thumbnail pertama ke ImageView lama
                placeholder(R.color.grey)
                error(R.drawable.ic_error_image)
            }
        }

        // b. Nama Produk
        namaProduk.text = produk.nama

        // c. Harga Produk
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        hargaProduk.text = formatRupiah.format(produk.harga).replace("Rp", "Rp ").trim()

// d. Status Stok
        stockStatus.text = "Tersedia: ${produk.stok} Buah" // <-- BINDING BARU
        stockStatus.setTextColor(resources.getColor(
            if (produk.stok > 0) R.color.Secondary_1 else R.color.red,
            null
        )) // Beri warna merah jika stok 0 (asumsi ada warna merah)

        // e. Tombol Cart (Hanya teks aksi)
        btnCart.text = "Tambah ke Keranjang"
        btnCart.isEnabled = produk.stok > 0 // Nonaktifkan tombol jika stok habis
    }

    private fun setupRatingInfo(view: View, produk: Produk) {
        val ratingText = view.findViewById<TextView>(R.id.rating_text)
        val reviewCount = view.findViewById<TextView>(R.id.review_count)

        ratingText.text = String.format(Locale.US, "%.1f", produk.rating)
        reviewCount.text = "(${produk.terjual} terjual, 10 Ulasan)"
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_detailproduk)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_cart) {
                findNavController().navigate(R.id.action_detailProdukFragment_to_CartFragment)
                true
            } else false
        }
    }

    private fun setupViewPager(view: View) {
        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        viewPager.adapter = DetailPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Deskripsi"
                1 -> "Spesifikasi"
                2 -> "Ulasan"
                else -> ""
            }
        }.attach()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailProdukFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}