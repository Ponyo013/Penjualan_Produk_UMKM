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
import subItemPesananAdapter
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class PesananAdapter : ListAdapter<Pesanan, PesananAdapter.PesananViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val binding = ItemRowPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PesananViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PesananViewHolder(private val binding: ItemRowPesananBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pesanan: Pesanan) {
            binding.tvOrderId.text = "Order ID: #${pesanan.id}"
            binding.tvOrderAddress.text = "Alamat: ${pesanan.user.alamat}"
            binding.tvOrderExpedition.text = "Expedisi: ${pesanan.ekspedisi?.nama} - Est ${pesanan.ekspedisi?.estimasiHari} hari"

            val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            numberFormat.maximumFractionDigits = 0  // Hapus ,00
            numberFormat.currency = Currency.getInstance("IDR")

            // Ongkir
            val ongkirFormatted = numberFormat.format(pesanan.ekspedisi?.biaya)
            binding.tvOrderOngkir.text = "Ongkir: ${ongkirFormatted.replace("Rp", "Rp ")}"

            // Total harga termasuk ongkir
            val total = pesanan.totalHarga + (pesanan.ekspedisi?.biaya ?: 0.0)
            val totalFormatted = numberFormat.format(total)
            binding.tvOrderTotal.text = "Total: ${totalFormatted.replace("Rp", "Rp ")}"

            // Tanggal
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
            binding.tvOrderDate.text = pesanan.tanggal.format(formatter)

            // Sub-item RecyclerView dengan subtotal
            val subItemAdapter = subItemPesananAdapter(pesanan.items)
            binding.rvOrderedItems.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = subItemAdapter
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pesanan>() {
            override fun areItemsTheSame(oldItem: Pesanan, newItem: Pesanan) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Pesanan, newItem: Pesanan) = oldItem == newItem
        }
    }
}

