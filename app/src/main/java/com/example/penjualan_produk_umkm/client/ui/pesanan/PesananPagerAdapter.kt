package com.example.penjualan_produk_umkm.client.ui.pesanan

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
// FIX: Gunakan import dari Firestore Model
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan

class PesananPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val status = when (position) {
            0 -> StatusPesanan.DIPROSES
            1 -> StatusPesanan.DIKIRIM
            2 -> StatusPesanan.SELESAI
            else -> throw IllegalStateException("Invalid position")
        }
        return PesananListFragment.newInstance(status)
    }
}