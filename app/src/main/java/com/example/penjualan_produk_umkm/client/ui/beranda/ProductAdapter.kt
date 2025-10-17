// File: com/example/penjualan_produk_umkm/client/ui/beranda/ProductAdapter.kt

package com.example.penjualan_produk_umkm.client.ui.beranda

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.model.Produk
import java.text.NumberFormat
import java.util.*
import coil.load

class ProductAdapter(
    private val products: List<Produk>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val productImage: ImageView = itemView.findViewById(R.id.product_image) // <-- BARU: Untuk Coil
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val productRating: TextView = itemView.findViewById(R.id.product_rating)
        val productSoldCount: TextView = itemView.findViewById(R.id.product_sold_count) // <-- BARU: Untuk jumlah Terjual

        init {
            // Diagnostik: Cek apakah TextView berhasil ditemukan
            if (productName == null || productPrice == null || productRating == null) {
                Log.e("AdapterError", "Satu atau lebih TextView tidak ditemukan! Periksa ID di item_product.xml")
            }
        }
        @SuppressLint("SetTextI18n")
        fun bind(produk: Produk, onItemClick: (Int) -> Unit) {

            // 1. Mengisi Nama Produk
            productName.text = produk.nama

            // 2. Mengisi Rating
            productRating.text = String.format(Locale.US, "%.1f", produk.rating)

            // 3. Mengisi Harga (Format Rupiah)
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatRupiah.maximumFractionDigits = 0 // <--- TAMBAHKAN BARIS KRITIS INI

            val formattedPrice = formatRupiah.format(produk.harga).replace("Rp", "Rp ").trim()
            productPrice.text = formattedPrice
            // 4. Mengisi INFORMASI TERJUAL
            productSoldCount.text = "Terjual ${produk.terjual}" // <-- PENAMBAHAN BARU

// 1. Dapatkan ID Resource Gambar Pertama (Int)
            val firstImageId = produk.gambarResourceIds.firstOrNull()

            // 2. MUAT GAMBAR MENGGUNAKAN COIL
            if (firstImageId != null) {
                // KOREKSI: Panggil load() dengan Int tunggal (firstImageId)
                productImage.load(firstImageId) {
                    placeholder(R.color.grey)
                    error(R.drawable.ic_error_image)
                    crossfade(true)
                }
            } else {
                // Jika list kosong, tampilkan placeholder default
                productImage.setImageResource(R.drawable.ic_empty_star) // Asumsi ic_empty_star ada
            }


            // 6. Navigasi ke Detail Produk
            itemView.setOnClickListener {
                onItemClick(produk.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position], onItemClick)
    }

    override fun getItemCount() = products.size
}