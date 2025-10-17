package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.model.Produk // Import model Produk
import java.io.Serializable // Untuk mengirim objek dalam Bundle

private const val ARG_PRODUK = "produkObject"

class DeskripsiFragment : Fragment(R.layout.fragment_deskripsi) {

    private var produk: Produk? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Ambil objek Produk dari Bundle
            produk = it.getSerializable(ARG_PRODUK) as? Produk
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deskripsiTextView = view.findViewById<TextView>(R.id.deskripsi_produk)

        // Isi deskripsi menggunakan data dari objek Produk
        produk?.let { p ->
            // Pastikan ID judul_produk ada di fragment_deskripsi.xml
            val judulTextView = view.findViewById<TextView>(R.id.judul_produk)
            judulTextView.text = "Deskripsi Produk"

            deskripsiTextView.text = p.deskripsi
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(produk: Produk?) =
            DeskripsiFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PRODUK, produk as Serializable)
                }
            }
    }
}