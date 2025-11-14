package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel

private const val ARG_PRODUK_ID = "produkId"

class SpesifikasiFragment : Fragment(R.layout.fragment_spesifikasi) {

    private var produkId: Int? = null
    private lateinit var viewModel: ProdukViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            produkId = it.getInt(ARG_PRODUK_ID)
        }

        val db = AppDatabase.getDatabase(requireContext())
        val factory = ViewModelFactory(produkDao = db.produkDao())
        viewModel = viewModels<ProdukViewModel> { factory }.value
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headingTextView = view.findViewById<TextView>(R.id.heading_spesifikasi)
        val container = view.findViewById<LinearLayout>(R.id.spesifikasi_container)

        produkId?.let { id ->
            viewModel.allProduk.observe(viewLifecycleOwner) { produkList ->
                val produk = produkList.find { it.id == id }

                produk?.let { p ->
                    headingTextView.text = "Detail Spesifikasi Produk"
                    populateSpesifikasiTable(container, p.spesifikasi, layoutInflater)
                } ?: run {
                    headingTextView.text = "Data spesifikasi tidak tersedia."
                }
            }
        }
    }

    private fun populateSpesifikasiTable(
        container: LinearLayout,
        spesifikasiString: String,
        inflater: LayoutInflater
    ) {
        val lines = spesifikasiString.split("\n")
        for (line in lines) {
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                val label = parts[0].trim()
                val value = parts[1].trim()

                val rowView = inflater.inflate(R.layout.item_spesifikasi_row, container, false)
                val labelView = rowView.findViewById<TextView>(R.id.spec_label)
                val valueView = rowView.findViewById<TextView>(R.id.spec_value)

                labelView.text = label
                valueView.text = value

                container.addView(rowView)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(produkId: Int) =
            SpesifikasiFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUK_ID, produkId)
                }
            }
    }
}
