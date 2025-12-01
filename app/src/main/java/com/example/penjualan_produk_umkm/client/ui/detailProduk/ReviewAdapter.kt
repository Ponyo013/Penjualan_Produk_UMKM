package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.firestore.model.Ulasan
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(
    private var reviews: List<Ulasan>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reviewerName: TextView = itemView.findViewById(R.id.reviewer_name)
        private val reviewComment: TextView = itemView.findViewById(R.id.review_comment)
        private val reviewRatingText: TextView = itemView.findViewById(R.id.review_rating_text)
        private val reviewDate: TextView = itemView.findViewById(R.id.review_date)

        fun bind(ulasan: Ulasan) {
            reviewerName.text = ulasan.userName
            reviewComment.text = ulasan.komentar

            val starString = buildString {
                repeat(ulasan.rating.toInt()) { append("★") }
                repeat(5 - ulasan.rating.toInt()) { append("☆") }
            }
            reviewRatingText.text = "$starString ${"%.1f".format(ulasan.rating)}"

            // Format tanggal dari Timestamp Firestore
            ulasan.tanggal?.toDate()?.let { date ->
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
                reviewDate.text = formatter.format(date)
            } ?: run {
                reviewDate.text = ""
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size

    fun updateReviews(newReviews: List<Ulasan>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }
}
