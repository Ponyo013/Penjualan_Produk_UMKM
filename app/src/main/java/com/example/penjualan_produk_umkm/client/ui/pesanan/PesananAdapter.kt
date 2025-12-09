package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.example.penjualan_produk_umkm.databinding.ItemRowPesananBinding
import com.example.penjualan_produk_umkm.viewModel.PesananViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

class PesananAdapter(
    private val viewModel: PesananViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val onCancelClick: (Pesanan) -> Unit
) : ListAdapter<Pesanan, PesananAdapter.PesananViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val binding = ItemRowPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PesananViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val pesanan = getItem(position)
        holder.bind(pesanan, viewModel, lifecycleOwner, onCancelClick)
    }

    class PesananViewHolder(private val binding: ItemRowPesananBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pesanan: Pesanan, viewModel: PesananViewModel, lifecycleOwner: LifecycleOwner, onCancelClick: (Pesanan) -> Unit) {

            // 1. Setup Data Dasar Pesanan
            binding.tvOrderId.text = "Order ID: #${pesanan.id.take(8).uppercase()}"

            // Format Rupiah
            val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
                currency = Currency.getInstance("IDR")
            }

            // Tampilkan Total Harga
            val totalFormatted = numberFormat.format(pesanan.totalHarga)
            binding.tvOrderTotal.text = "Total: ${totalFormatted.replace("Rp", "Rp ")}"
            binding.tvOrderAddress.text = "Alamat: ${pesanan.alamat}"

            binding.tvOrderExpedition.text = "Ekspedisi: ${pesanan.ekspedisiId}"


            // Tanggal
            try {
                val date = pesanan.tanggal.toDate()
                val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                binding.tvOrderDate.text = formatter.format(date)
            } catch (e: Exception) {
                binding.tvOrderDate.text = "-"
            }

            // Placeholder awal sebelum data item dimuat
            binding.tvOrderOngkir.text = "Memuat..."

            // 2. Observasi Item untuk Menghitung Subtotal & Ongkir
            viewModel.getItemsForPesanan(pesanan.id).observe(lifecycleOwner) { items ->
                // Setup RecyclerView untuk item produk
                val subItemAdapter = SubItemPesananAdapter(items)
                binding.rvOrderedItems.apply {
                    layoutManager = LinearLayoutManager(itemView.context)
                    adapter = subItemAdapter
                    setHasFixedSize(true)
                }

                // --- [LOGIKA FIX ONGKIR] ---
                // Hitung subtotal harga barang (Harga x Jumlah)
                val subtotalBarang = items.sumOf { it.produkHarga * it.jumlah }

                // Hitung Ongkir = Total Bayar - Subtotal Barang
                // (Gunakan max(0.0) untuk mencegah minus karena floating point error)
                val ongkir = (pesanan.totalHarga - subtotalBarang).coerceAtLeast(0.0)

                // Update UI Ongkir
                binding.tvOrderOngkir.text = "Ongkir: ${numberFormat.format(ongkir).replace("Rp", "Rp ")}"
                if (pesanan.status == StatusPesanan.DIPROSES.name) {
                    binding.btnCancelOrder.visibility = View.VISIBLE
                    binding.btnCancelOrder.setOnClickListener {
                        onCancelClick(pesanan) // Panggil callback ke Fragment
                    }
                } else {
                    binding.btnCancelOrder.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pesanan>() {
            override fun areItemsTheSame(oldItem: Pesanan, newItem: Pesanan) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Pesanan, newItem: Pesanan) =
                oldItem == newItem
        }
    }
}