package com.example.penjualan_produk_umkm.client.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.databinding.ItemRecentlyViewedBinding
import java.text.NumberFormat
import java.util.Locale

class RecentlyViewedAdapter(
    private var products: List<Produk>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<RecentlyViewedAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRecentlyViewedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentlyViewedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produk = products[position]
        with(holder.binding) {
            tvRecentName.text = produk.nama

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatRupiah.maximumFractionDigits = 0
            tvRecentPrice.text = formatRupiah.format(produk.harga)

            tvRecentRating.text = produk.rating.toString()

            if (produk.gambarUrl.isNotEmpty()) {
                ivRecentImage.load(produk.gambarUrl) {
                    crossfade(true)
                    placeholder(R.color.grey)
                }
            } else {
                ivRecentImage.setImageResource(R.drawable.ic_error_image)
            }

            root.setOnClickListener { onClick(produk.id) }
        }
    }

    override fun getItemCount() = products.size

    fun updateData(newProducts: List<Produk>) {
        products = newProducts
        notifyDataSetChanged()
    }
}