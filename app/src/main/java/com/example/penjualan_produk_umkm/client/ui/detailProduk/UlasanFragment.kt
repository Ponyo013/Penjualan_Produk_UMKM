package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.app.AlertDialog
import android.os.Bundle
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
import com.example.penjualan_produk_umkm.database.firestore.model.Ulasan
import com.example.penjualan_produk_umkm.database.firestore.model.User
import com.example.penjualan_produk_umkm.viewModel.UlasanViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PRODUK_ID = "produkId"

class UlasanFragment : Fragment(R.layout.fragment_ulasan) {

    private var produkId: String? = null
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var starIcons: List<ImageView>
    private lateinit var recyclerView: RecyclerView

    private val ulasanViewModel: UlasanViewModel by viewModels { ViewModelFactory() }

    private var currentUserName: String? = null
    private var currentUserId: String? = null
    private var currentRating: Int = 5 // Default rating 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            produkId = it.getString(ARG_PRODUK_ID)
        }

        // Ambil info user
        val auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUserId!!)
                .get()
                .addOnSuccessListener { doc ->
                    val user = doc.toObject(User::class.java)
                    currentUserName = user?.nama
                }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_reviews)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // FIX: Inisialisasi Adapter dengan Parameter Lengkap (List, ID User, Callback Delete)
        reviewAdapter = ReviewAdapter(
            reviews = emptyList(),
            currentUserId = currentUserId,
            onDeleteClick = { ulasan ->
                showDeleteConfirmation(ulasan)
            }
        )
        recyclerView.adapter = reviewAdapter

        setupStarRating(view)

        produkId?.let { id ->
            // Load data
            ulasanViewModel.getUlasanByProdukId(id)

            // Observe perubahan data
            ulasanViewModel.ulasanList.observe(viewLifecycleOwner) { reviews ->
                reviewAdapter.updateReviews(reviews)
            }

            setupKirimButton(view, id)
        }
    }

    // --- LOGIKA BINTANG ---
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

    // --- LOGIKA KIRIM ---
    private fun setupKirimButton(view: View, produkId: String) {
        val buttonKirim = view.findViewById<Button>(R.id.button_kirim)
        val editTextComment = view.findViewById<EditText>(R.id.edit_text_comment)

        buttonKirim.setOnClickListener {
            val comment = editTextComment.text.toString().trim()

            if (comment.isEmpty()) {
                Toast.makeText(context, "Mohon isi komentar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUserId == null) {
                Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newReview = Ulasan(
                id = "",
                produkId = produkId,
                userId = currentUserId!!,
                userName = currentUserName ?: "Pengguna",
                rating = currentRating.toFloat(),
                komentar = comment,
                tanggal = Timestamp.now()
            )

            ulasanViewModel.insertUlasan(newReview)

            // Reset UI setelah kirim
            editTextComment.setText("")
            setRating(5)
            Toast.makeText(context, "Ulasan terkirim!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- LOGIKA DELETE (BARU) ---
    private fun showDeleteConfirmation(ulasan: Ulasan) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Ulasan")
            .setMessage("Apakah Anda yakin ingin menghapus ulasan ini?")
            .setPositiveButton("Hapus") { dialog, _ ->
                // Panggil ViewModel untuk hapus
                ulasanViewModel.deleteUlasan(
                    ulasan = ulasan,
                    onSuccess = {
                        Toast.makeText(context, "Ulasan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    },
                    onError = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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