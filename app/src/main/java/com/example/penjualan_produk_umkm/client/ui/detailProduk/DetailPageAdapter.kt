package com.example.penjualan_produk_umkm.client.ui.detailProduk

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class DetailPagerAdapter(
    fragment: Fragment,
    private val produkId: String // <--- FIX: Terima ID String dari Constructor
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            // Kirim ID String ke masing-masing Fragment
            0 -> DeskripsiFragment.newInstance(produkId)
            1 -> SpesifikasiFragment.newInstance(produkId)
            2 -> UlasanFragment.newInstance(produkId)
            else -> throw IllegalStateException("Invalid position")
        }
    }
}