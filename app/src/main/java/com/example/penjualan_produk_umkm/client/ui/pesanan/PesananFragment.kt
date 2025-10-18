package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.FragmentPesananBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PesananFragment : Fragment() {

    private var _binding: FragmentPesananBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPesananBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = PesananPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        val tabTitles = listOf("Diproses", "Dikirim", "Selesai")

        // Pasang TabLayoutMediator dengan custom view untuk font & ukuran
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val tabView = LayoutInflater.from(requireContext())
                .inflate(R.layout.tab_custom, null) as TextView
            tabView.text = tabTitles[position]
            tab.customView = tabView
        }.attach()

        // Listener untuk ubah warna teks tab aktif / tidak aktif
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                (tab.customView as? TextView)?.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.Secondary_2)
                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                (tab.customView as? TextView)?.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_secondary)
                )
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Set tab pertama aktif warna
        (binding.tabLayout.getTabAt(binding.viewPager.currentItem)?.customView as? TextView)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.Secondary_2))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
