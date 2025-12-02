package com.example.penjualan_produk_umkm.client.ui.beranda

import android.annotation.SuppressLint
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
import java.util.*

class ProductAdapter(
    private var products: List<Produk>,
    private val onItemClick: (String) -> Unit // FIX: ID is now String
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productRating: TextView = itemView.findViewById(R.id.product_rating)
        private val productSoldCount: TextView = itemView.findViewById(R.id.product_sold_count)

        @SuppressLint("SetTextI18n")
        fun bind(produk: Produk) {
            // Nama produk
            productName.text = produk.nama

            // Rating produk
            productRating.text = String.format(Locale.US, "%.1f", produk.rating)

            // Harga (format Rupiah)
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
                maximumFractionDigits = 0
            }
            val formattedPrice = formatRupiah.format(produk.harga).replace("Rp", "Rp ").trim()
            productPrice.text = formattedPrice

            // Jumlah terjual
            productSoldCount.text = "Terjual ${produk.terjual}"

            // FIX: Load Image from URL (Firestore)
            // We use R.drawable.ic_error_image as placeholder if URL is empty
            if (produk.gambarUrl.isNotEmpty()) {
                productImage.load(produk.gambarUrl) {
                    placeholder(R.color.grey)
                    error(R.drawable.ic_error_image)
                    crossfade(true)
                }
            } else {
                // If you want to support legacy local images for testing, you might need logic here,
                // but for pure Firestore migration, use placeholder.
                productImage.setImageResource(R.drawable.ic_error_image)
            }

            // Klik item
            itemView.setOnClickListener { onItemClick(produk.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Produk>) {
        this.products = newProducts
        notifyDataSetChanged()
    }
}