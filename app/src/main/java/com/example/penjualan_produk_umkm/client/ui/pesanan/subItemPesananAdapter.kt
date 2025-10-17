package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.SubItemProdukPesananBinding
import com.example.penjualan_produk_umkm.model.ItemPesanan

class SubItemPesananAdapter(private val items: List<ItemPesanan>) : RecyclerView.Adapter<SubItemPesananAdapter.SubItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubItemViewHolder {
        val binding = SubItemProdukPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SubItemViewHolder(private val binding: SubItemProdukPesananBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemPesanan) {
            binding.tvProductName.text = "- ${item.produk.nama}"
            binding.tvProductQuantity.text = "x${item.jumlah}"

            // Set placeholder if image URL is missing, otherwise load the image
            if (item.produk.gambarUrl.isNullOrEmpty()) {
                val placeholder = ContextCompat.getDrawable(itemView.context, R.drawable.shape_image_placeholder)
                binding.ivProductImage.setImageDrawable(placeholder)
            } else {
                // Later, you can add your image loading logic here (e.g., using Coil)
                // For now, it will also show the placeholder
                val placeholder = ContextCompat.getDrawable(itemView.context, R.drawable.shape_image_placeholder)
                binding.ivProductImage.setImageDrawable(placeholder)
            }
        }
    }
}
