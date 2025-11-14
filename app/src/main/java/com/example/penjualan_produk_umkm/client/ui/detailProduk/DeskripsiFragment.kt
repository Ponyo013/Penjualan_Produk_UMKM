package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel

private const val ARG_PRODUK_ID = "produkId"

class DeskripsiFragment : Fragment(R.layout.fragment_deskripsi) {

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

        val deskripsiTextView = view.findViewById<TextView>(R.id.deskripsi_produk)
        val judulTextView = view.findViewById<TextView>(R.id.judul_produk)

        // Ambil data produk dari database berdasarkan ID
        produkId?.let { id ->
            viewModel.allProduk.observe(viewLifecycleOwner) { produkList ->
                val produk = produkList.find { it.id == id }
                produk?.let { p ->
                    judulTextView.text = p.nama
                    deskripsiTextView.text = p.deskripsi
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(produkId: Int) =
            DeskripsiFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUK_ID, produkId)
                }
            }
    }
}
