package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.example.penjualan_produk_umkm.R

// Tambahkan ARG_PRODUK jika Anda akan mengirim ID produk ke sini
private const val ARG_PRODUK_ID = "productId"

class UlasanInputFragment : Fragment() {

    private var currentRating: Int = 4 // Default rating awal
    private lateinit var starIcons: List<ImageView>
    private var productId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productId = it.getInt(ARG_PRODUK_ID) // Ambil ID produk jika dikirim
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ulasan_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi daftar ImageView bintang
        starIcons = listOf(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        )

        // Set listener untuk setiap bintang
        starIcons.forEachIndexed { index, star ->
            star.setOnClickListener {
                setRating(index + 1) // index + 1 karena indeks mulai dari 0
            }
        }

        // Atur rating awal (misalnya 4 bintang, sesuai gambar)
        setRating(currentRating)

        val editTextComment = view.findViewById<EditText>(R.id.edit_text_comment)
        val buttonKirim = view.findViewById<Button>(R.id.button_kirim)

        // Set placeholder sesuai gambar
        editTextComment.hint = "Cukup awet, model unik"

        buttonKirim.setOnClickListener {
            val comment = editTextComment.text.toString()
            val rating = currentRating

            // TODO: Di sini Anda akan mengirim data rating dan komentar ke server
            // Untuk simulasi UI/UX, kita bisa langsung kembali dan berpura-pura data terkirim.

            // Untuk demo, kita bisa mencetak ke logcat
            println("Product ID: $productId, Rating: $rating, Comment: $comment")

            // Setelah mengirim, kembali ke halaman Detail Produk atau Ulasan
            findNavController().popBackStack()
        }
    }

    // Fungsi untuk mengatur tampilan bintang berdasarkan rating
    private fun setRating(rating: Int) {
        currentRating = rating
        starIcons.forEachIndexed { index, star ->
            if (index < rating) {
                star.setImageResource(R.drawable.ic_full_star)
            } else {
                star.setImageResource(R.drawable.ic_empty_star)
            }
        }
    }

    // NewInstance untuk mengirim productId jika diperlukan
    companion object {
        @JvmStatic
        fun newInstance(productId: Int) =
            UlasanInputFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUK_ID, productId)
                }
            }
    }
}