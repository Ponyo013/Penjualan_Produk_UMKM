package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.databinding.ItemRowPesananBinding
import com.example.penjualan_produk_umkm.viewModel.PesananViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

class PesananAdapter(
    private val viewModel: PesananViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<Pesanan, PesananAdapter.PesananViewHolder>(DIFF_CALLBACK) {

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

        fun bind(pesanan: Pesanan, viewModel: PesananViewModel, lifecycleOwner: LifecycleOwner) {

            // Order ID
            binding.tvOrderId.text = "Order ID: #${pesanan.id.takeLast(8).uppercase()}" // Ambil 8 karakter terakhir ID

            // Alamat (Perlu diambil dari User/Firestore, tapi sementara kita hardcode atau ambil dari field jika ada)
            // Idealnya, Pesanan menyimpan "alamatSnapshot" saat checkout.
            // Jika tidak, kita harus query User lagi (tapi di Adapter tidak disarankan).
            // SEMENTARA: Tampilkan placeholder atau ambil dari ViewModel jika ViewModel support.
            binding.tvOrderAddress.text = "Alamat Pengiriman"

            // Ekspedisi (Perlu query atau simpan snapshot nama ekspedisi di Pesanan)
            // SEMENTARA: Tampilkan placeholder
            binding.tvOrderExpedition.text = "Ekspedisi (Estimasi - hari)"

            // Format Rupiah
            val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
                currency = Currency.getInstance("IDR")
            }

            // Ongkir & Total (Ambil langsung dari objek Pesanan jika sudah dihitung)
            // Note: Di model Pesanan baru kita simpan totalHarga final.
            val totalFormatted = numberFormat.format(pesanan.totalHarga)
            binding.tvOrderTotal.text = "Total: ${totalFormatted.replace("Rp", "Rp ")}"

            // Ongkir (Jika tidak disimpan terpisah, mungkin sulit ditampilkan, sementara hide atau 0)
            binding.tvOrderOngkir.text = "Ongkir: -"

            // Tanggal (Konversi dari Timestamp Firebase)
            try {
                val date = pesanan.tanggal.toDate()
                val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                binding.tvOrderDate.text = formatter.format(date)
            } catch (e: Exception) {
                binding.tvOrderDate.text = "-"
            }

            // Sub-item RecyclerView (Ambil dari ViewModel Firebase)
            viewModel.getItemsForPesanan(pesanan.id).observe(lifecycleOwner) { items ->
                val subItemAdapter = SubItemPesananAdapter(items)
                binding.rvOrderedItems.apply {
                    layoutManager = LinearLayoutManager(itemView.context)
                    adapter = subItemAdapter
                    setHasFixedSize(true)
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