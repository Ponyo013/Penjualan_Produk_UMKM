package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.model.Ulasan
import com.example.penjualan_produk_umkm.viewModel.ProdukViewModel
import com.example.penjualan_produk_umkm.viewModel.UlasanViewModel
import org.threeten.bp.LocalDate

private const val ARG_PRODUK_ID = "produkId"

class UlasanFragment : Fragment(R.layout.fragment_ulasan) {

    private var produkId: Int? = null
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var starIcons: List<ImageView>

    // ViewModel untuk ambil data ulasan
    private lateinit var ulasanViewModel: UlasanViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            produkId = it.getInt(ARG_PRODUK_ID)
        }

        val db = AppDatabase.getDatabase(requireContext())
        val factory = ViewModelFactory(db = db)
        ulasanViewModel = viewModels<UlasanViewModel> { factory }.value
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStarRating(view)
        produkId?.let { id ->
            setupReviewList(view, id)
            setupKirimButton(view, id)
        }
    }

    private var currentRating: Int = 4

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

    private fun setupReviewList(view: View, produkId: Int) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_reviews)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.isNestedScrollingEnabled = false

        val db = AppDatabase.getDatabase(requireContext())
        val userDao = db.userDao()

        // Observe users
        userDao.getAllUsers().observe(viewLifecycleOwner) { users ->
            // Observe ulasan untuk produk ini
            ulasanViewModel.getUlasanByProdukId(produkId).observe(viewLifecycleOwner) { reviews ->
                // Init adapter dengan data real dari database
                reviewAdapter = ReviewAdapter(reviews, users)
                recyclerView.adapter = reviewAdapter
            }
        }
    }

    private fun setupKirimButton(view: View, produkId: Int) {
        val buttonKirim = view.findViewById<Button>(R.id.button_kirim)
        val editTextComment = view.findViewById<EditText>(R.id.edit_text_comment)

        buttonKirim.setOnClickListener {
            val comment = editTextComment.text.toString().trim()
            if (comment.isNotEmpty() && currentRating > 0) {
                val newReview = Ulasan(
                    id = 0, // 0 biarkan auto-generate di database
                    produkId = produkId,
                    userId = 1, // sementara user dummy
                    rating = currentRating.toFloat(),
                    komentar = comment,
                    tanggal = LocalDate.now()
                )
                ulasanViewModel.insertUlasan(newReview)

                // Reset input
                editTextComment.setText("")
                setRating(5)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(produkId: Int) =
            UlasanFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUK_ID, produkId)
                }
            }
    }
}
