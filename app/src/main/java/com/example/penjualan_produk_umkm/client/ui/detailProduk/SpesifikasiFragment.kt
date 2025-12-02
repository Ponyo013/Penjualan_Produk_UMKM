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
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel

private const val ARG_PRODUK_ID = "produkId"

class SpesifikasiFragment : Fragment(R.layout.fragment_spesifikasi) {

    // FIX: ID Produk sekarang String
    private var produkId: String? = null

    // FIX: Gunakan Factory kosong
    private val viewModel: ProdukViewModel by viewModels {
        ViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // FIX: Ambil String
            produkId = it.getString(ARG_PRODUK_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headingTextView = view.findViewById<TextView>(R.id.heading_spesifikasi)
        val container = view.findViewById<LinearLayout>(R.id.spesifikasi_container)

        produkId?.let { id ->
            // Ambil data spesifik dari Firestore
            viewModel.getProdukById(id) { produk ->
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
        container.removeAllViews() // Pastikan bersih sebelum menambah row
        val lines = spesifikasiString.split("\n")
        for (line in lines) {
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                val label = parts[0].trim()
                val value = parts[1].trim()

                val rowView = inflater.inflate(R.layout.item_spesifikasi_row, container, false)
                val labelView = rowView.findViewById<TextView>(R.id.spec_label)
                val valueView = rowView.findViewById<TextView>(R.id.spec_value)

                // Tambahkan titik dua agar rapi
                labelView.text = "$label :"
                valueView.text = value

                container.addView(rowView)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(produkId: String) = // FIX: Parameter String
            SpesifikasiFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRODUK_ID, produkId) // FIX: putString
                }
            }
    }
}