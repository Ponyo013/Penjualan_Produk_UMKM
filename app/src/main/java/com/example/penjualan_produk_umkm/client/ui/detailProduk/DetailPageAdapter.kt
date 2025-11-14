package com.example.penjualan_produk_umkm.client.ui.detailProduk

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class DetailPagerAdapter(fragment: DetailProdukFragment) : FragmentStateAdapter(fragment) {

    // Ambil ID produk dari DetailProdukFragment
    private val produkId = fragment.produk?.id

    private val fragments = listOf(
        // Oper ID produk, bukan objek Produk
        DeskripsiFragment.newInstance(produkId ?: 0),
        SpesifikasiFragment.newInstance(produkId ?: 0),
        UlasanFragment.newInstance(produkId ?: 0)
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
