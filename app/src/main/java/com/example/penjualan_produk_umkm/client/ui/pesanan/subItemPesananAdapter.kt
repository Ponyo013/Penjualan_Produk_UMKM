package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.databinding.SubItemProdukPesananBinding
import java.text.NumberFormat
import java.util.*

class SubItemPesananAdapter(private val items: List<ItemPesanan>) :
    RecyclerView.Adapter<SubItemPesananAdapter.SubItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubItemViewHolder {
        val binding = SubItemProdukPesananBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SubItemViewHolder(private val binding: SubItemProdukPesananBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemPesanan) {
            val numberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
                maximumFractionDigits = 0
            }

            binding.tvProductName.text = item.produkNama // Pastikan field ini ada di Model ItemPesanan
            binding.tvProductQuantity.text = "x${item.jumlah}"

            val hargaFormatted = numberFormat.format(item.produkHarga)
            binding.tvProductPrice.text = hargaFormatted.replace("Rp", "Rp ")

            val subtotal = item.produkHarga * item.jumlah
            val subtotalFormatted = numberFormat.format(subtotal)
            binding.tvProductSubtotal.text = subtotalFormatted.replace("Rp", "Rp ")

            // Gambar (Placeholder dulu karena ItemPesanan Firestore biasanya ga simpan URL gambar)
            binding.ivProductImage.setImageResource(R.drawable.ic_error_image)
        }
    }
}