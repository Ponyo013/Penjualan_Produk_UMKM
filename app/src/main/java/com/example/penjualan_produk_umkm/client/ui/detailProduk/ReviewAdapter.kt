package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.firestore.model.Ulasan
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(
    private var reviews: List<Ulasan>,
    private val currentUserId: String?, // ID user yang login
    private val onDeleteClick: (Ulasan) -> Unit // Callback saat tombol hapus diklik
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewerName: TextView = itemView.findViewById(R.id.reviewer_name)
        val reviewComment: TextView = itemView.findViewById(R.id.review_comment)
        val reviewRatingText: TextView = itemView.findViewById(R.id.review_rating_text)
        val reviewDate: TextView = itemView.findViewById(R.id.review_date)

        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_review)

        fun bind(ulasan: Ulasan, userIdLogin: String?, onDeleteClick: (Ulasan) -> Unit) {
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

            try {
                val date = ulasan.tanggal.toDate()
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                reviewDate.text = formatter.format(date)
            } catch (e: Exception) {
                reviewDate.text = "-"
            }

            if (userIdLogin != null && ulasan.userId == userIdLogin) {
                btnDelete.visibility = View.VISIBLE
                btnDelete.setOnClickListener {
                    onDeleteClick(ulasan)
                }
            } else {
                btnDelete.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        // Kita oper currentUserId dan onDeleteClick dari Class Adapter ke dalam fungsi bind ViewHolder
        holder.bind(reviews[position], currentUserId, onDeleteClick)
    }

    override fun getItemCount() = reviews.size

    fun updateReviews(newReviews: List<Ulasan>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }
}