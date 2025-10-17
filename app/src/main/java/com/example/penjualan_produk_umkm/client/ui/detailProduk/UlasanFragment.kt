package com.example.penjualan_produk_umkm.client.ui.detailProduk
import java.util.*// Tambahkan import ini untuk formatting tanggal
import java.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.model.Produk
import com.example.penjualan_produk_umkm.model.Ulasan // Pastikan model Ulasan diimpor
import com.example.penjualan_produk_umkm.ulasanList // Akses ke list ulasan global
import com.example.penjualan_produk_umkm.dummyUsers
import com.example.penjualan_produk_umkm.model.User
import java.io.Serializable
import org.threeten.bp.LocalDate // Pastikan Anda mengimpor LocalDate dari ThreeTenABP
import org.threeten.bp.format.DateTimeFormatter
import kotlin.collections.MutableList // Penting untuk casting add()

private const val ARG_PRODUK = "produkObject"

class UlasanFragment : Fragment(R.layout.fragment_ulasan) {

    private var produk: Produk? = null
    private var currentRating: Int = 4 // State rating saat ini (default 4)
    private lateinit var reviewAdapter: ReviewAdapter // Asumsi Anda punya ReviewAdapter
    private lateinit var starIcons: List<ImageView>
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            produk = it.getSerializable(ARG_PRODUK) as? Produk
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        produk?.let { p ->
            setupStarRating(view)
            setupReviewList(view, p.id)
            setupKirimButton(view, p.id)
        }
    }

    // Fungsi untuk inisialisasi bintang dan listener
    private fun setupStarRating(view: View) {
        starIcons = listOf(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        )

        starIcons.forEachIndexed { index, star ->
            star.setOnClickListener {
                setRating(index + 1)
            }
        }
        setRating(currentRating) // Atur rating awal 4 bintang
    }

    // Fungsi untuk mengatur tampilan bintang
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

    // Fungsi untuk menyiapkan RecyclerView daftar ulasan
    private fun setupReviewList(view: View, produkId: Int) {
        // 1. Inisialisasi RecyclerView
        recyclerView = view.findViewById(R.id.recycler_reviews)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 2. Persiapan Data User (Mengubah Kunci Map dari Email ke User ID (String))
        // Kunci Map harus berupa ID user (1, 2, 3...) dalam format String agar bisa dicocokkan
        val usersByIdInt: Map<Int, User> = dummyUsers.values.associateBy { it.id }

        val reviewsForThisProduct = ulasanList.filter { it.produkId == produkId }

        // 4. MENGHILANGKAN KONFLIK DAN MENGGUNAKAN ADAPTER HANYA SEKALI
        // MENGHILANGKAN ERROR: Return type mismatch: expected 'String', actual 'Int'
        // Asumsi ReviewAdapter membutuhkan Map<String, User>
        reviewAdapter = ReviewAdapter(reviewsForThisProduct, usersByIdInt) // <-- Panggil dengan Map<Int, User>
        recyclerView.adapter = reviewAdapter
    }

    // Fungsi untuk mengatur tombol KIRIM
    private fun setupKirimButton(view: View, produkId: Int) {
        val buttonKirim = view.findViewById<Button>(R.id.button_kirim)
        val editTextComment = view.findViewById<EditText>(R.id.edit_text_comment)

        buttonKirim.setOnClickListener {
            val comment = editTextComment.text.toString().trim()
            val rating = currentRating.toFloat()

            // 1. Validasi Input
            if (comment.isNotEmpty() && rating > 0) {

                // 2. SIMULASI: Buat Objek Ulasan Baru
                // ID harus unik (kita gunakan ukuran list + 1)
                val newReview = Ulasan(
                    id = ulasanList.size + 1, // ID otomatis Int
                    produkId = produkId,
                    userId = 99, // User ID dummy untuk pengujian
                    rating = rating,
                    komentar = comment,
                    tanggal = LocalDate.now()
                )

                // 3. SIMULASI PERSISTENCE: Tambahkan ke List Global
                // Karena ulasanList adalah MutableList, ini akan berhasil.
                (ulasanList as MutableList).add(newReview)

                // 4. REFRESH UI: Ambil daftar ulasan yang sudah diperbarui dan kirim ke adapter
                val updatedReviews = ulasanList.filter { it.produkId == produkId }
                reviewAdapter.updateReviews(updatedReviews) // <-- Ini yang me-refresh RecyclerView

                // 5. Bersihkan Input (UX)
                editTextComment.setText("")
                setRating(5) // Reset rating ke 5 bintang

                // Opsional: Tampilkan feedback ke user (Toast)
                // Toast.makeText(context, "Ulasan berhasil ditambahkan!", Toast.LENGTH_SHORT).show()

            } else {
                // Toast.makeText(context, "Rating dan Komentar wajib diisi.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    companion object {
        @JvmStatic
        fun newInstance(produk: Produk?): UlasanFragment {
            return UlasanFragment().apply {
                arguments = Bundle().apply {
                    if (produk != null) {
                        putSerializable(ARG_PRODUK, produk as Serializable)
                    }
                }
            }
        }
    }
}