package com.example.penjualan_produk_umkm.client.ui.beranda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import java.text.NumberFormat
import java.util.Locale

class RecommendationAdapter(
    private var products: List<Produk>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.iv_product_image)
        val name: TextView = itemView.findViewById(R.id.tv_product_name)
        val price: TextView = itemView.findViewById(R.id.tv_product_price)
        val rating: TextView = itemView.findViewById(R.id.tv_rating)
        val sold: TextView = itemView.findViewById(R.id.tv_sold)

        fun bind(produk: Produk) {
            name.text = produk.nama
            rating.text = produk.rating.toString()
            sold.text = "${produk.terjual} Terjual"

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatRupiah.maximumFractionDigits = 0
            price.text = formatRupiah.format(produk.harga)

            if (produk.gambarUrl.isNotEmpty()) {
                img.load(produk.gambarUrl) {
                    crossfade(true)
                    placeholder(R.color.grey)
                }
            } else {
                img.setImageResource(R.drawable.ic_error_image)
            }

            itemView.setOnClickListener { onItemClick(produk.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateData(newProducts: List<Produk>) {
        products = newProducts
        notifyDataSetChanged()
    }
}