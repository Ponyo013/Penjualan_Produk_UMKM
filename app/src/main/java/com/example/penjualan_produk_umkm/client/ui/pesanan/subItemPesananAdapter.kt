package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load // Pastikan import coil ada
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan // Gunakan Model Firestore
import com.example.penjualan_produk_umkm.databinding.SubItemProdukPesananBinding
import java.text.NumberFormat
import java.util.*

// Ubah parameter dari List<ItemPesananWithProduk> menjadi List<ItemPesanan>
class SubItemPesananAdapter(private val items: List<ItemPesanan>) :
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

        @SuppressLint("SetTextI18n")
        fun bind(item: ItemPesanan) {
            val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
            }

            // Nama produk (Diambil dari snapshot item pesanan)
            binding.tvProductName.text = item.produkNama

            // Jumlah item
            binding.tvProductQuantity.text = "x${item.jumlah}"

            // Harga per produk
            val hargaFormatted = numberFormat.format(item.produkHarga)
            binding.tvProductPrice.text = hargaFormatted.replace("Rp", "Rp ")

            // Subtotal
            val subtotal = item.produkHarga * item.jumlah
            val subtotalFormatted = numberFormat.format(subtotal)
            binding.tvProductSubtotal.text = subtotalFormatted.replace("Rp", "Rp ")
            if (item.gambarUrl.isNotEmpty()) {
                binding.ivProductImage.load(item.gambarUrl) {
                    crossfade(true)
                    placeholder(R.color.grey) // Warna abu-abu saat loading
                    error(R.drawable.ic_error_image) // Gambar default jika error
                }
            } else {
                // Fallback jika URL kosong
                binding.ivProductImage.setImageResource(R.drawable.ic_error_image)
            }
        }
    }
}