package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.databinding.ItemRowPesananBinding
import com.example.penjualan_produk_umkm.model.Pesanan
import org.threeten.bp.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale

class PesananAdapter : ListAdapter<Pesanan, PesananAdapter.PesananViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val binding = ItemRowPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PesananViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val pesanan = getItem(position)
        holder.bind(pesanan)
    }

    class PesananViewHolder(private val binding: ItemRowPesananBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pesanan: Pesanan) {
            binding.tvOrderId.text = "Order ID: #${pesanan.id}"
            binding.tvOrderStatus.text = "Status: ${pesanan.status}"

            // Format total price to Rupiah currency format
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            numberFormat.maximumFractionDigits = 0
            val formattedPrice = numberFormat.format(pesanan.totalHarga)

            binding.tvOrderTotal.text = "Total: ${formattedPrice.replace("Rp", "Rp ")}"

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
            binding.tvOrderDate.text = pesanan.tanggal.format(formatter)

            // Setup sub-item RecyclerView
            val subItemAdapter = SubItemPesananAdapter(pesanan.items)
            binding.rvOrderedItems.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = subItemAdapter
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pesanan>() {
            override fun areItemsTheSame(oldItem: Pesanan, newItem: Pesanan): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Pesanan, newItem: Pesanan): Boolean {
                return oldItem == newItem
            }
        }
    }
}
