package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.model.Ulasan
import com.example.penjualan_produk_umkm.database.model.User
import com.example.penjualan_produk_umkm.viewModel.UlasanViewModel
import org.threeten.bp.LocalDate

private const val ARG_PRODUK_ID = "produkId"

class UlasanFragment : Fragment(R.layout.fragment_ulasan) {

    private var produkId: Int? = null
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var starIcons: List<ImageView>
    private lateinit var recyclerView: RecyclerView

    // ViewModel
    private lateinit var ulasanViewModel: UlasanViewModel

    // Variabel penampung data sementara
    private var listUser: List<User> = emptyList()
    private var listUlasan: List<Ulasan> = emptyList()

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

        recyclerView = view.findViewById(R.id.recycler_reviews)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Inisialisasi Adapter kosong dulu agar tidak error
        reviewAdapter = ReviewAdapter(emptyList(), emptyList())
        recyclerView.adapter = reviewAdapter

        setupStarRating(view)

        produkId?.let { id ->
            Log.d("UlasanFragment", "Loading ulasan untuk Produk ID: $id")
            setupDataObservation(id)
            setupKirimButton(view, id)
        }
    }

    // --- PERBAIKAN UTAMA ADA DI SINI ---
    private fun setupDataObservation(id: Int) {
        val db = AppDatabase.getDatabase(requireContext())
        val userDao = db.userDao()

        // 1. Ambil Data User (Secara Independen)
        userDao.getAllUsers().observe(viewLifecycleOwner) { users ->
            Log.d("UlasanFragment", "Data Users didapat: ${users.size} user")
            listUser = users
            updateUI() // Coba update UI setiap data user berubah
        }

        // 2. Ambil Data Ulasan (Secara Independen)
        ulasanViewModel.getUlasanByProdukId(id).observe(viewLifecycleOwner) { reviews ->
            Log.d("UlasanFragment", "Data Ulasan didapat: ${reviews.size} ulasan")
            listUlasan = reviews
            updateUI() // Coba update UI setiap data ulasan berubah
        }
    }

    private fun updateUI() {
        // Kita update adapter hanya jika data user DAN data ulasan sudah siap (opsional)
        // Atau update saja langsung apa adanya.
        if (listUlasan.isNotEmpty()) {
            // Update data di adapter
            // Pastikan ReviewAdapter punya fungsi updateReviews atau buat instance baru
            reviewAdapter = ReviewAdapter(listUlasan, listUser)
            recyclerView.adapter = reviewAdapter
        } else {
            Log.d("UlasanFragment", "List ulasan masih kosong")
        }
    }
    // -----------------------------------

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

    private fun setupKirimButton(view: View, produkId: Int) {
        val buttonKirim = view.findViewById<Button>(R.id.button_kirim)
        val editTextComment = view.findViewById<EditText>(R.id.edit_text_comment)

        buttonKirim.setOnClickListener {
            val comment = editTextComment.text.toString().trim()

            // Validasi input
            if (comment.isEmpty()) {
                Toast.makeText(context, "Mohon isi komentar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newReview = Ulasan(
                id = 0,
                produkId = produkId,
                userId = 1, // Hardcode sementara user 1 (Owner) atau 2 (Pembeli)
                rating = currentRating.toFloat(),
                komentar = comment,
                tanggal = LocalDate.now()
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
        fun newInstance(produkId: Int) =
            UlasanFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUK_ID, produkId)
                }
            }
    }
}