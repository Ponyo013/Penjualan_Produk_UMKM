package com.example.penjualan_produk_umkm.client.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.BottomSheetFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

class FilterBottomSheetFragment(
    private val onApplyClick: (String, String, Double, Double) -> Unit // Callback
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol Reset
        binding.btnReset.setOnClickListener {
            binding.cgSort.check(R.id.chip_terbaru)
            binding.cgCategory.check(R.id.chip_cat_all)
            binding.etMinPrice.text.clear()
            binding.etMaxPrice.text.clear()
        }

        // Tombol Terapkan
        binding.btnApply.setOnClickListener {
            // 1. Ambil Sort
            val selectedSortId = binding.cgSort.checkedChipId
            val sortOption = when(selectedSortId) {
                R.id.chip_terlaris -> "TERLARIS"
                R.id.chip_harga_rendah -> "HARGA_MURAH"
                R.id.chip_harga_tinggi -> "HARGA_MAHAL"
                else -> "TERBARU"
            }

            // 2. Ambil Kategori
            val selectedCatId = binding.cgCategory.checkedChipId
            val selectedCatChip = binding.cgCategory.findViewById<Chip>(selectedCatId)
            val category = selectedCatChip?.text?.toString() ?: "Semua"

            // 3. Ambil Harga
            val minPrice = binding.etMinPrice.text.toString().toDoubleOrNull() ?: 0.0
            val maxPrice = binding.etMaxPrice.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE

            // Kirim data balik ke SearchFragment
            onApplyClick(sortOption, category, minPrice, maxPrice)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}