package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.model.Ulasan
import com.example.penjualan_produk_umkm.database.model.User
import java.util.*
import org.threeten.bp.format.DateTimeFormatter

class ReviewAdapter(
    private var reviews: List<Ulasan>,
    private val users: List<User>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    // Membuat map userId: User agar cepat lookup
    private val userMap: Map<Int, User> = users.associateBy { it.id }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewerName: TextView = itemView.findViewById(R.id.reviewer_name)
        val reviewComment: TextView = itemView.findViewById(R.id.review_comment)
        val reviewRatingText: TextView = itemView.findViewById(R.id.review_rating_text)
        val reviewDate: TextView = itemView.findViewById(R.id.review_date)

        fun bind(ulasan: Ulasan, userMap: Map<Int, User>) {
            val reviewerNameText = userMap[ulasan.userId]?.nama ?: "Anonymous User"
            reviewerName.text = reviewerNameText
            reviewComment.text = ulasan.komentar

            val starString = buildString {
                repeat(ulasan.rating.toInt()) { append("★") }
                repeat(5 - ulasan.rating.toInt()) { append("☆") }
            }
            reviewRatingText.text = starString + String.format(Locale.US, " %.1f", ulasan.rating)

            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("in", "ID"))
            reviewDate.text = ulasan.tanggal.format(formatter)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position], userMap)
    }

    override fun getItemCount() = reviews.size

    fun updateReviews(newReviews: List<Ulasan>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }
}
