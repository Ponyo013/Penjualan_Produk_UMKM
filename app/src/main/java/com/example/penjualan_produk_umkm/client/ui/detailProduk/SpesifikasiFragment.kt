package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView // Wajib untuk TextView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.model.Produk // Wajib untuk model Produk
import java.io.Serializable // Wajib untuk menerima Bundle

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SpesifikasiFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val ARG_PRODUK = "produkObject"
class SpesifikasiFragment : Fragment(R.layout.fragment_spesifikasi) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var produk: Produk? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Mengambil objek Produk dari Bundle menggunakan key ARG_PRODUK
            produk = it.getSerializable(ARG_PRODUK) as? Produk
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headingTextView = view.findViewById<TextView>(R.id.heading_spesifikasi)
        val container = view.findViewById<LinearLayout>(R.id.spesifikasi_container) // Ambil container baru

        produk?.let { p ->
            headingTextView.text = "Detail Spesifikasi Produk"

            // Panggil fungsi untuk mengisi tabel spesifikasi
            populateSpesifikasiTable(container, p.spesifikasi, layoutInflater)

        } ?: run {
            headingTextView.text = "Data spesifikasi tidak tersedia."
        }
    }

    // FUNGSI BARU: Memecah string dan mengisi LinearLayout
    private fun populateSpesifikasiTable(container: LinearLayout, spesifikasiString: String, inflater: LayoutInflater) {

        // 1. Pecah string berdasarkan baris baru (\n)
        val lines = spesifikasiString.split("\n")

        for (line in lines) {
            // 2. Pecah setiap baris menjadi Kunci dan Nilai (menggunakan pemisah ':')
            val parts = line.split(":", limit = 2)

            if (parts.size == 2) {
                val label = parts[0].trim()
                val value = parts[1].trim()

                // 3. Inflate layout baris
                val rowView = inflater.inflate(R.layout.item_spesifikasi_row, container, false)

                // 4. Isi TextView di dalam baris
                val labelView = rowView.findViewById<TextView>(R.id.spec_label)
                val valueView = rowView.findViewById<TextView>(R.id.spec_value)

                labelView.text = label
                valueView.text = value

                // 5. Tambahkan baris ke container
                container.addView(rowView)
            }
        }
    }
    companion object {
        // Factory method untuk menerima objek Produk (Wajib untuk PagerAdapter)
        @JvmStatic
        fun newInstance(produk: Produk?) =
            SpesifikasiFragment().apply {
                arguments = Bundle().apply {
                    if (produk != null) {
                        putSerializable(ARG_PRODUK, produk as Serializable)
                    }
                }
            }
    }
}