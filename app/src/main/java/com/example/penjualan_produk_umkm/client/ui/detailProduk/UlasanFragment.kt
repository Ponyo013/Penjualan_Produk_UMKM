package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.firestore.model.Ulasan
import com.example.penjualan_produk_umkm.viewModel.UlasanViewModel
import com.google.firebase.Timestamp

private const val ARG_PRODUK_ID = "produkId"

class UlasanFragment : Fragment(R.layout.fragment_ulasan) {

    private var produkId: String? = null
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var starIcons: List<ImageView>
    private lateinit var recyclerView: RecyclerView

    private val ulasanViewModel: UlasanViewModel by viewModels()

    private var listUlasan: List<Ulasan> = emptyList()

    private var currentRating: Int = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            produkId = it.getString(ARG_PRODUK_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_reviews)
        recyclerView.layoutManager = LinearLayoutManager(context)

        reviewAdapter = ReviewAdapter(emptyList()) // Sesuaikan constructor ReviewAdapter
        recyclerView.adapter = reviewAdapter

        setupStarRating(view)
        setupKirimButton(view)

        // Load ulasan dari Firestore
        produkId?.let { id ->
            Log.d("UlasanFragment", "Loading ulasan untuk Produk ID: $id")
            ulasanViewModel.getUlasanByProdukId(id)
            ulasanViewModel.ulasanList.observe(viewLifecycleOwner) { reviews ->
                listUlasan = reviews
                reviewAdapter.updateReviews(listUlasan)
            }
        }
    }

    private fun setupStarRating(view: View) {
        starIcons = listOf(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        )
        starIcons.forEachIndexed { index, star ->
            star.setOnClickListener { setRating(index + 1) }
        }
        setRating(currentRating)
    }

    private fun setRating(rating: Int) {
        currentRating = rating
        starIcons.forEachIndexed { index, star ->
            if (index < rating) star.setImageResource(R.drawable.ic_full_star)
            else star.setImageResource(R.drawable.ic_empty_star)
        }
    }

    private fun setupKirimButton(view: View) {
        val buttonKirim = view.findViewById<Button>(R.id.button_kirim)
        val editTextComment = view.findViewById<EditText>(R.id.edit_text_comment)

        buttonKirim.setOnClickListener {
            val comment = editTextComment.text.toString().trim()
            if (comment.isEmpty()) {
                Toast.makeText(context, "Mohon isi komentar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newReview = Ulasan(
                id = "",
                produkId = produkId ?: "",
                userId = "user123", // Ganti sesuai user login
                rating = currentRating.toFloat(),
                komentar = comment,
                tanggal = Timestamp.now()
            )
            ulasanViewModel.insertUlasan(newReview)

            // Reset input
            editTextComment.setText("")
            setRating(5)
            Toast.makeText(context, "Ulasan terkirim!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(produkId: String) =
            UlasanFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRODUK_ID, produkId)
                }
            }
    }
}
