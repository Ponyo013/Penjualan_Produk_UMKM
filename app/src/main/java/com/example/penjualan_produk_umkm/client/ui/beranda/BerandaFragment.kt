package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.LinearLayout
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.uiComponent.SearchBar

class BerandaFragment : Fragment(R.layout.fragment_beranda) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter

    private val viewModel: ProdukViewModel by viewModels {
        ViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)
        recyclerView = view.findViewById(R.id.recycler_popular_products)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = ProductAdapter(emptyList()) { productId ->
            val bundle = Bundle().apply { putString("productId", productId) }
            findNavController().navigate(R.id.action_BerandaFragment_to_detailProdukFragment, bundle)
        }
        recyclerView.adapter = adapter

        // Search Bar (Compose)
        val composeView = view.findViewById<ComposeView>(R.id.compose_search_bar)
        composeView.setContent {
            SearchBar(
                onSearch = {
                    findNavController().navigate(R.id.action_global_to_searchFragment)
                }
            )
        }

        // Navigasi kategori
        view.findViewById<LinearLayout>(R.id.cat_sparepart).setOnClickListener {
            navigateToCategory("Spare Parts")
        }
        view.findViewById<LinearLayout>(R.id.cat_aksesoris).setOnClickListener {
            navigateToCategory("Aksesoris")
        }
        view.findViewById<LinearLayout>(R.id.cat_sepeda_induk).setOnClickListener {
            navigateToCategory("Sepeda")
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()

        setupNavigationClick(view.findViewById(R.id.notification_icon), R.id.action_berandaFragment_to_notificationFragment)
        setupNavigationClick(view.findViewById(R.id.cart_icon), R.id.action_BerandaFragment_to_CartFragment)
    }

    private fun setupObservers() {
        viewModel.allProduk.observe(viewLifecycleOwner, Observer { produkList ->
            // filter produk terlaris (>= 0 terjual)
            val popularProducts = produkList.filter { it.terjual >= 0 }
            adapter.updateProducts(popularProducts)
        })
    }

    private fun navigateToCategory(category: String) {
        val bundle = Bundle().apply { putString("parentCategoryName", category) }
        findNavController().navigate(R.id.action_BerandaFragment_to_categoryListFragment, bundle)
    }

    private fun setupNavigationClick(view: View, destinationId: Int) {
        view.setOnClickListener {
            findNavController().navigate(destinationId)
        }
    }
}
