package com.example.penjualan_produk_umkm.client.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.databinding.ItemRecentSearchBinding

class RecentSearchAdapter(
    private var items: MutableList<String>,
    private val onClick: (String) -> Unit,
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRecentSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val query = items[position]
        holder.binding.tvHistoryText.text = query

        holder.itemView.setOnClickListener { onClick(query) }
        holder.binding.btnRemoveHistory.setOnClickListener {
            onRemove(query)
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}