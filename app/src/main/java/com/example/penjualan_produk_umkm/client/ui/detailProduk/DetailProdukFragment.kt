package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.dummyItems
import com.example.penjualan_produk_umkm.model.ItemPesanan
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
    internal var produk: Produk? = null

    private lateinit var btnAddToCart: Button
    private lateinit var quantityControls: MaterialCardView
    private lateinit var btnIncrease: ImageButton
    private lateinit var btnDecrease: ImageButton
    private lateinit var tvQuantity: TextView

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

        val productId = arguments?.getInt("productId")
        produk = produkDummyList.find { it.id == productId }

        produk?.let { p ->
            setupToolbar(view)
            setupProductInfo(view, p)
            setupRatingInfo(view, p)
            setupViewPager(view)
            updateCartControls(p)
        } ?: run {
            findNavController().popBackStack()
        }
    }

    private fun setupProductInfo(view: View, produk: Produk) {
        val namaProduk = view.findViewById<TextView>(R.id.nama_produk)
        val hargaProduk = view.findViewById<TextView>(R.id.harga_produk)
        val stockStatus = view.findViewById<TextView>(R.id.stock_status)
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart)
        quantityControls = view.findViewById(R.id.quantity_controls)
        btnIncrease = view.findViewById(R.id.btn_increase_quantity)
        btnDecrease = view.findViewById(R.id.btn_decrease_quantity)
        tvQuantity = view.findViewById(R.id.tv_quantity)

        val galleryViewPager = view.findViewById<ViewPager2>(R.id.gallery_view_pager)
        galleryViewPager.adapter = GalleryAdapter(produk.gambarResourceIds)

        namaProduk.text = produk.nama
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        hargaProduk.text = formatRupiah.format(produk.harga).replace("Rp", "Rp ").trim()
        stockStatus.text = "Tersedia: ${produk.stok} Buah"
        stockStatus.setTextColor(resources.getColor(if (produk.stok > 0) R.color.Secondary_1 else R.color.red, null))

        btnAddToCart.isEnabled = produk.stok > 0

        btnAddToCart.setOnClickListener {
            dummyItems.add(ItemPesanan(produk = produk, jumlah = 1))
            Toast.makeText(requireContext(), "Produk ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
            updateCartControls(produk)
        }

        btnIncrease.setOnClickListener {
            val existingItem = dummyItems.find { it.produk.id == produk.id }
            existingItem?.let { it.jumlah++ }
            updateCartControls(produk)
        }

        btnDecrease.setOnClickListener {
            val existingItem = dummyItems.find { it.produk.id == produk.id }
            existingItem?.let {
                if (it.jumlah > 1) {
                    it.jumlah--
                } else {
                    dummyItems.remove(it)
                }
            }
            updateCartControls(produk)
        }
    }

    private fun updateCartControls(produk: Produk) {
        val existingItem = dummyItems.find { it.produk.id == produk.id }
        if (existingItem != null) {
            btnAddToCart.visibility = View.GONE
            quantityControls.visibility = View.VISIBLE
            tvQuantity.text = existingItem.jumlah.toString()
        } else {
            btnAddToCart.visibility = View.VISIBLE
            quantityControls.visibility = View.GONE
        }
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
        val adapter = DetailPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Deskripsi"
                1 -> "Spesifikasi"
                2 -> "Ulasan"
                else -> ""
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                adjustViewPagerHeight(viewPager, adapter)
            }
        })

        view.post {
            adjustViewPagerHeight(viewPager, adapter)
        }

        val ratingContainer = view.findViewById<FrameLayout>(R.id.rating)
        ratingContainer.setOnClickListener {
            viewPager.setCurrentItem(2, true)
        }
    }

    private fun adjustViewPagerHeight(viewPager: ViewPager2, adapter: DetailPagerAdapter) {
        val position = viewPager.currentItem
        val fragmentTag = "f" + position
        val currentFragment = childFragmentManager.findFragmentByTag(fragmentTag)

        if (currentFragment?.view == null) {
            return
        }

        viewPager.postDelayed({
            currentFragment.view?.let { fragmentView ->
                fragmentView.measure(
                    View.MeasureSpec.makeMeasureSpec(viewPager.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val height = fragmentView.measuredHeight
                if (height > 0 && height != viewPager.height) {
                    viewPager.layoutParams = viewPager.layoutParams.apply {
                        this.height = height + 16
                    }
                }
            }
        }, 50)
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