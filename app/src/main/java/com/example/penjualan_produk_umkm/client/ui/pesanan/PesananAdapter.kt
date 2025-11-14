package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.databinding.ItemRowPesananBinding
import com.example.penjualan_produk_umkm.database.relation.PesananWithItems
import com.example.penjualan_produk_umkm.viewModel.PesananViewModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import org.threeten.bp.format.DateTimeFormatter

class PesananAdapter(
    private val viewModel: PesananViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<PesananWithItems, PesananAdapter.PesananViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val binding = ItemRowPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PesananViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val pesanan = getItem(position)
        holder.bind(pesanan, viewModel, lifecycleOwner)
    }

    class PesananViewHolder(private val binding: ItemRowPesananBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pesananWithItems: PesananWithItems, viewModel: PesananViewModel, lifecycleOwner: LifecycleOwner) {
            val pesanan = pesananWithItems.pesanan

            // Order ID
            binding.tvOrderId.text = "Order ID: #${pesanan.id}"

            // Alamat
            val user = pesananWithItems.user
            binding.tvOrderAddress.text = "Alamat: ${user?.alamat ?: "-"}"

            // Ekspedisi
            val ekspedisi = pesananWithItems.ekspedisi
            binding.tvOrderExpedition.text =
                "Expedisi: ${ekspedisi?.nama ?: "-"} - Est ${ekspedisi?.estimasiHari ?: "-"} hari"

            // Format Rupiah
            val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
                currency = Currency.getInstance("IDR")
            }

            // Ongkir
            val ongkirFormatted = numberFormat.format(ekspedisi?.biaya ?: 0.0)
            binding.tvOrderOngkir.text = "Ongkir: ${ongkirFormatted.replace("Rp", "Rp ")}"

            // Total harga termasuk ongkir
            val total = pesanan.totalHarga + (ekspedisi?.biaya ?: 0.0)
            val totalFormatted = numberFormat.format(total)
            binding.tvOrderTotal.text = "Total: ${totalFormatted.replace("Rp", "Rp ")}"

            // Tanggal
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
            binding.tvOrderDate.text = pesanan.tanggal.format(formatter)

            // Sub-item RecyclerView
            viewModel.getItemsForPesanan(pesanan.id).observe(lifecycleOwner) { itemsWithProduk ->
                val subItemAdapter = SubItemPesananAdapter(itemsWithProduk)
                binding.rvOrderedItems.apply {
                    layoutManager = LinearLayoutManager(itemView.context)
                    adapter = subItemAdapter
                    setHasFixedSize(true)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PesananWithItems>() {
            override fun areItemsTheSame(oldItem: PesananWithItems, newItem: PesananWithItems) =
                oldItem.pesanan.id == newItem.pesanan.id

            override fun areContentsTheSame(oldItem: PesananWithItems, newItem: PesananWithItems) =
                oldItem == newItem
        }
    }
}
