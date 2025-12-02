package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
// FIX: Gunakan Model Firestore
import com.example.penjualan_produk_umkm.database.firestore.model.Ulasan
import java.text.SimpleDateFormat
import java.util.*

// Hapus parameter 'users: List<User>'
class ReviewAdapter(
    private var reviews: List<Ulasan>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    // Hapus userMap karena kita tidak butuh lookup manual lagi

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewerName: TextView = itemView.findViewById(R.id.reviewer_name)
        val reviewComment: TextView = itemView.findViewById(R.id.review_comment)
        val reviewRatingText: TextView = itemView.findViewById(R.id.review_rating_text)
        val reviewDate: TextView = itemView.findViewById(R.id.review_date)

        fun bind(ulasan: Ulasan) {
            // FIX: Ambil nama langsung dari objek Ulasan (jika ada field userName)
            // Atau tampilkan ID sebagian jika nama null
            // Pastikan di Model Ulasan Anda sudah menambahkan field 'var userName: String? = null'
            val nameDisplay = if (!ulasan.userName.isNullOrEmpty()) {
                ulasan.userName
            } else {
                "User ${ulasan.userId.take(5)}..."
            }

            reviewerName.text = nameDisplay
            reviewComment.text = ulasan.komentar

            val starString = buildString {
                repeat(ulasan.rating.toInt()) { append("★") }
                repeat(5 - ulasan.rating.toInt()) { append("☆") }
            }
            reviewRatingText.text = "$starString ${String.format(Locale.US, "%.1f", ulasan.rating)}"

            // FIX: Format Tanggal dari Timestamp Firebase
            try {
                val date = ulasan.tanggal.toDate() // Konversi Timestamp ke Date
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                reviewDate.text = formatter.format(date)
            } catch (e: Exception) {
                reviewDate.text = "-"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position]) // Hapus parameter userMap
    }

    override fun getItemCount() = reviews.size

    fun updateReviews(newReviews: List<Ulasan>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }
}