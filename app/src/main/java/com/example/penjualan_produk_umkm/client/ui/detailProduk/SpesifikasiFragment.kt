package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.util.Log
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

    private var produkId: String? = null

    private val viewModel: ProdukViewModel by viewModels {
        ViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            produkId = it.getString(ARG_PRODUK_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headingTextView = view.findViewById<TextView>(R.id.heading_spesifikasi)
        val container = view.findViewById<LinearLayout>(R.id.spesifikasi_container)

        produkId?.let { id ->
            viewModel.getProdukById(id) { produk ->
                produk?.let {
                    p ->
                    headingTextView.text = "Detail Spesifikasi Produk"
                    Log.d("SPESIFIKASI", "DATA: ${p.spesifikasi}")
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
        if (spesifikasiString.isBlank()) return

        // FIX: Split by comma or newline to support both old and new data format
        val pairs = spesifikasiString.split("[,\n]".toRegex()).filter { it.isNotBlank() }

        for (pair in pairs) {
            val parts = pair.split(":", limit = 2)
            if (parts.size == 2) {
                val label = parts[0].trim()
                val value = parts[1].trim()
                if (label.isNotBlank() || value.isNotBlank()) {
                    val rowView = inflater.inflate(R.layout.item_spesifikasi_row, container, false)
                    val labelView = rowView.findViewById<TextView>(R.id.spec_label)
                    val valueView = rowView.findViewById<TextView>(R.id.spec_value)
                    labelView.text = label
                    valueView.text = value
                    container.addView(rowView)
                }
            } else if (parts.size == 1 && parts[0].isNotBlank()) {
                val rowView = inflater.inflate(R.layout.item_spesifikasi_row, container, false)
                val labelView = rowView.findViewById<TextView>(R.id.spec_label)
                val valueView = rowView.findViewById<TextView>(R.id.spec_value)
                labelView.text = parts[0].trim()
                valueView.visibility = View.GONE
                container.addView(rowView)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(produkId: String) =
            SpesifikasiFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRODUK_ID, produkId)
                }
            }
    }
}