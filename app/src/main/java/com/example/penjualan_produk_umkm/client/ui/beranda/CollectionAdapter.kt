package com.example.penjualan_produk_umkm.client.ui.beranda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.model.Koleksi

class CollectionAdapter(
    private val collections: List<Koleksi>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CollectionAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.iv_collection_image)
        val title: TextView = itemView.findViewById(R.id.tv_collection_title)

        fun bind(item: Koleksi) {
            title.text = item.title
            img.setImageResource(item.imageResId)
            itemView.setOnClickListener { onClick(item.categoryFilter) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(collections[position])
    }

    override fun getItemCount() = collections.size
}