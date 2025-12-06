package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.model.Artikel

class ArtikelAdapter(private val articles: List<Artikel>) :
    RecyclerView.Adapter<ArtikelAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.iv_article_image)
        val title: TextView = itemView.findViewById(R.id.tv_article_title)

        fun bind(artikel: Artikel, position: Int) {
            title.text = artikel.title
            img.load(artikel.imageUrl) {
                crossfade(true)
            }

            // ... Logic Span Size (Grid) tetap sama ...
            val params = img.layoutParams as ConstraintLayout.LayoutParams
            if (position == 0) {
                params.dimensionRatio = "2:1"
                title.textSize = 18f
            } else {
                params.dimensionRatio = "1:1"
                title.textSize = 12f
            }
            img.layoutParams = params

            // --- UPDATE CLICK LISTENER ---
            itemView.setOnClickListener {
                val bundle = Bundle().apply {
                    putInt("ARG_ID", artikel.id)
                    putString("ARG_TITLE", artikel.title)
                    putString("ARG_IMAGE", artikel.imageUrl)
                }

                // Navigasi ke ArticleDetailFragment membawa Bundle
                it.findNavController().navigate(
                    R.id.action_BerandaFragment_to_articleDetailFragment,
                    bundle
                )
            }
        }
    }
    // ... onCreateViewHolder & getItemCount tetap sama ...
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artikel, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(articles[position], position)
    }
    override fun getItemCount() = articles.size
}