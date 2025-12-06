package com.example.penjualan_produk_umkm.client.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.BottomSheetFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!

    // Callback untuk mengirim hasil balik ke SearchFragment
    private var onApplyListener: ((String, String, Double, Double) -> Unit)? = null

    fun setOnApplyListener(listener: (String, String, Double, Double) -> Unit) {
        this.onApplyListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. BACA DATA YANG DIKIRIM (Untuk Fix Bug Reset)
        val currentSort = arguments?.getString("ARG_SORT") ?: "TERBARU"
        val currentCat = arguments?.getString("ARG_CAT") ?: "Semua"
        val currentMin = arguments?.getDouble("ARG_MIN", 0.0) ?: 0.0
        val currentMax = arguments?.getDouble("ARG_MAX", Double.MAX_VALUE) ?: Double.MAX_VALUE

        // 2. SET UI SESUAI DATA TERAKHIR
        // Set Sort Chip
        val sortChipId = when (currentSort) {
            "TERLARIS" -> R.id.chip_terlaris
            "HARGA_MURAH" -> R.id.chip_harga_rendah
            "HARGA_MAHAL" -> R.id.chip_harga_tinggi
            else -> R.id.chip_terbaru
        }
        binding.cgSort.check(sortChipId)

        // Set Category Chip
        // Pastikan ID chip di XML bottom_sheet_filter.xml sesuai dengan ini
        val catChipId = when (currentCat) {
            "Sepeda" -> R.id.chip_cat_sepeda
            "Spare Parts" -> R.id.chip_cat_sparepart
            "Aksesoris" -> R.id.chip_cat_aksesoris
            "Perawatan" -> R.id.chip_cat_perawatan
            else -> R.id.chip_cat_all
        }
        binding.cgCategory.check(catChipId)

        // Set Price Inputs
        if (currentMin > 0) binding.etMinPrice.setText(currentMin.toLong().toString())
        if (currentMax < Double.MAX_VALUE) binding.etMaxPrice.setText(currentMax.toLong().toString())

        // 3. LOGIKA TOMBOL
        binding.btnReset.setOnClickListener {
            binding.cgSort.check(R.id.chip_terbaru)
            binding.cgCategory.check(R.id.chip_cat_all)
            binding.etMinPrice.text.clear()
            binding.etMaxPrice.text.clear()
        }

        binding.btnApply.setOnClickListener {
            // Ambil Sort
            val selectedSortId = binding.cgSort.checkedChipId
            val sortOption = when(selectedSortId) {
                R.id.chip_terlaris -> "TERLARIS"
                R.id.chip_harga_rendah -> "HARGA_MURAH"
                R.id.chip_harga_tinggi -> "HARGA_MAHAL"
                else -> "TERBARU"
            }

            // Ambil Kategori
            val selectedCatId = binding.cgCategory.checkedChipId
            val category = when(selectedCatId) {
                R.id.chip_cat_sepeda -> "Sepeda"
                R.id.chip_cat_sparepart -> "Spare Parts"
                R.id.chip_cat_aksesoris -> "Aksesoris"
                R.id.chip_cat_perawatan -> "Perawatan"
                else -> "Semua"
            }

            // Ambil Harga
            val minPrice = binding.etMinPrice.text.toString().toDoubleOrNull() ?: 0.0
            val maxPrice = binding.etMaxPrice.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE

            // Kirim balik
            onApplyListener?.invoke(sortOption, category, minPrice, maxPrice)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Static Method untuk membuat Fragment dengan Data Awal
    companion object {
        fun newInstance(
            currentSort: String,
            currentCat: String,
            currentMin: Double,
            currentMax: Double
        ): FilterBottomSheetFragment {
            val fragment = FilterBottomSheetFragment()
            val args = Bundle().apply {
                putString("ARG_SORT", currentSort)
                putString("ARG_CAT", currentCat)
                putDouble("ARG_MIN", currentMin)
                putDouble("ARG_MAX", currentMax)
            }
            fragment.arguments = args
            return fragment
        }
    }
}