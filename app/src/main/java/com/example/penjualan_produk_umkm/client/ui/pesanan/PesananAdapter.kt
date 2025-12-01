package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.databinding.ItemRowPesananBinding
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class PesananAdapter(
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<PesananAdapter.PesananViewHolder>() {

    private val pesananList = mutableListOf<Pesanan>()
    private val itemsMap = mutableMapOf<String, List<ItemPesanan>>() // pesananId -> List<ItemPesanan>

    fun submitList(pesanan: List<Pesanan>, itemsPerPesanan: Map<String, List<ItemPesanan>>) {
        pesananList.clear()
        pesananList.addAll(pesanan)
        itemsMap.clear()
        itemsMap.putAll(itemsPerPesanan)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val binding = ItemRowPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PesananViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val pesanan = pesananList[position]
        val items = itemsMap[pesanan.id] ?: emptyList()
        holder.bind(pesanan, items)
    }

    override fun getItemCount(): Int = pesananList.size

    class PesananViewHolder(private val binding: ItemRowPesananBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pesanan: Pesanan, items: List<ItemPesanan>) {
            // Order ID
            binding.tvOrderId.text = "Order ID: #${pesanan.id}"

            // Ambil alamat user dari Firestore
            FirebaseFirestore.getInstance().collection("users")
                .document(pesanan.userId)
                .get()
                .addOnSuccessListener { doc ->
                    val alamat = doc.getString("alamat") ?: "-"
                    binding.tvOrderAddress.text = "Alamat: $alamat"
                }

            // Ambil ekspedisi dari Firestore
            if (!pesanan.ekspedisiId.isNullOrEmpty()) {
                FirebaseFirestore.getInstance().collection("ekspedisi")
                    .document(pesanan.ekspedisiId!!)
                    .get()
                    .addOnSuccessListener { doc ->
                        val nama = doc.getString("nama") ?: "-"
                        val estimasi = doc.getLong("estimasiHari") ?: 0
                        val biaya = doc.getDouble("biaya") ?: 0.0

                        binding.tvOrderExpedition.text = "Ekspedisi: $nama - Est $estimasi hari"

                        // Format Rupiah
                        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                            maximumFractionDigits = 0
                        }

                        binding.tvOrderOngkir.text = "Ongkir: ${numberFormat.format(biaya).replace("Rp", "Rp ")}"

                        val total = items.sumOf { it.produkHarga * it.jumlah } + biaya
                        binding.tvOrderTotal.text = "Total: ${numberFormat.format(total).replace("Rp", "Rp ")}"
                    }
            } else {
                binding.tvOrderExpedition.text = "Ekspedisi: -"
                val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                    maximumFractionDigits = 0
                }
                binding.tvOrderOngkir.text = "Ongkir: ${numberFormat.format(0.0).replace("Rp", "Rp ")}"
                val total = items.sumOf { it.produkHarga * it.jumlah }
                binding.tvOrderTotal.text = "Total: ${numberFormat.format(total).replace("Rp", "Rp ")}"
            }

            // Tanggal
            val formatter = java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            binding.tvOrderDate.text = formatter.format(pesanan.tanggal.toDate())

            // Sub-item RecyclerView
            val subItemAdapter = SubItemPesananAdapter(items)
            binding.rvOrderedItems.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = subItemAdapter
                setHasFixedSize(true)
            }
        }
    }
}
