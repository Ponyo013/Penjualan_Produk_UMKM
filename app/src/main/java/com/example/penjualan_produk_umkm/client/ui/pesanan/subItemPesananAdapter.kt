package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.SubItemProdukPesananBinding
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import java.text.NumberFormat
import java.util.*

class SubItemPesananAdapter(private val items: List<ItemPesananWithProduk>) :
    RecyclerView.Adapter<SubItemPesananAdapter.SubItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubItemViewHolder {
        val binding = SubItemProdukPesananBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SubItemViewHolder(private val binding: SubItemProdukPesananBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemWithProduk: ItemPesananWithProduk) {
            val item = itemWithProduk.itemPesanan
            val produk = itemWithProduk.produk

            val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
            }

            // Nama produk
            binding.tvProductName.text = produk.nama

            // Jumlah item
            binding.tvProductQuantity.text = "x${item.jumlah}"

            // Harga per produk
            val hargaFormatted = numberFormat.format(produk.harga)
            binding.tvProductPrice.text = hargaFormatted.replace("Rp", "Rp ")

            // Subtotal
            val subtotal = produk.harga * item.jumlah
            val subtotalFormatted = numberFormat.format(subtotal)
            binding.tvProductSubtotal.text = subtotalFormatted.replace("Rp", "Rp ")

            // Gambar produk
            if (produk.gambarResourceIds.isNotEmpty()) {
                binding.ivProductImage.load(produk.gambarResourceIds.first()) {
                    crossfade(true)
                    placeholder(R.drawable.shape_image_placeholder)
                }
            } else {
                binding.ivProductImage.setImageResource(R.drawable.shape_image_placeholder)
            }
        }
    }
}
