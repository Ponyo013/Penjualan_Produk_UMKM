// File: com/example/penjualan_produk_umkm/client/ui/detailProduk/DetailPagerAdapter.kt

package com.example.penjualan_produk_umkm.client.ui.detailProduk

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.penjualan_produk_umkm.model.Produk
class DetailPagerAdapter(fragment: DetailProdukFragment) : FragmentStateAdapter(fragment) {

    private val produk = fragment.produk

    // TAMBAH FRAGMENT ULASAN DI SINI
    private val fragments = listOf(
        DeskripsiFragment.newInstance(produk),
        // KOREKSI: Hapus dua argumen String yang menyebabkan error.
        SpesifikasiFragment.newInstance(produk),
        UlasanFragment.newInstance(produk)
    )

    override fun getItemCount(): Int = fragments.size // Sekarang menjadi 3

    override fun createFragment(position: Int): Fragment = fragments[position]
}