package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

        fun bind(artikel: Artikel) {
            title.text = artikel.title
            img.load(artikel.imageUrl) {
                crossfade(true)
            }

            itemView.setOnClickListener {
                val bundle = Bundle().apply {
                    putInt("ARG_ID", artikel.id)
                    putString("ARG_TITLE", artikel.title)
                    putString("ARG_IMAGE", artikel.imageUrl)
                }
                it.findNavController().navigate(
                    R.id.action_BerandaFragment_to_articleDetailFragment,
                    bundle
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artikel, parent, false)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount() = articles.size
}