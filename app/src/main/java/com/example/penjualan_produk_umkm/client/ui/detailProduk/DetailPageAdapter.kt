package com.example.penjualan_produk_umkm.client.ui.detailProduk

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class DetailPagerAdapter(
    fragment: Fragment,
    private val produkId: String
) : FragmentStateAdapter(fragment) {

    private val fragments = listOf(
        DeskripsiFragment.newInstance(produkId),
        SpesifikasiFragment.newInstance(produkId),
        UlasanFragment.newInstance(produkId)
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
