import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.databinding.SubItemProdukPesananBinding
import com.example.penjualan_produk_umkm.model.ItemPesanan
import java.text.NumberFormat
import java.util.*

class subItemPesananAdapter(private val items: List<ItemPesanan>) :
    RecyclerView.Adapter<subItemPesananAdapter.SubItemViewHolder>() {

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

        fun bind(item: ItemPesanan) {
            val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            numberFormat.maximumFractionDigits = 0  // Hilangkan ,00

            // Nama
            binding.tvProductName.text = item.produk.nama

            // Jumlah
            binding.tvProductQuantity.text = "x${item.jumlah}"

            // Harga per item
            val hargaFormatted = numberFormat.format(item.produk.harga)
            binding.tvProductPrice.text = hargaFormatted.replace("Rp", "Rp ")

            // Subtotal
            val subtotal = item.produk.harga * item.jumlah
            val subtotalFormatted = numberFormat.format(subtotal)
            binding.tvProductSubtotal.text = subtotalFormatted.replace("Rp", "Rp ")


            // Gambar dengan placeholder
            if (item.produk.gambarUrl.isEmpty()) {
                val placeholder = ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.shape_image_placeholder
                )
                binding.ivProductImage.setImageDrawable(placeholder)
            } else {
                // Untuk sementara tetap pakai placeholder
                val placeholder = ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.shape_image_placeholder
                )
                binding.ivProductImage.setImageDrawable(placeholder)
            }
        }
    }
}
