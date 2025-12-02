package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel

private const val ARG_PRODUK_ID = "produkId"

class DeskripsiFragment : Fragment(R.layout.fragment_deskripsi) {

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

        val deskripsiTextView = view.findViewById<TextView>(R.id.deskripsi_produk)
        val judulTextView = view.findViewById<TextView>(R.id.judul_produk)

        // Ambil data produk dari Firestore berdasarkan ID
        produkId?.let { id ->
            viewModel.getProdukById(id) { produk ->
                // Callback async dari Firebase
                produk?.let { p ->
                    judulTextView.text = p.nama
                    deskripsiTextView.text = p.deskripsi
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(produkId: String) = // FIX: Parameter String
            DeskripsiFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRODUK_ID, produkId) // FIX: putString
                }
            }
    }
}