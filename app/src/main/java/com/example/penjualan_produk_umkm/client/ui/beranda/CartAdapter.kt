package com.example.penjualan_produk_umkm.client.ui.beranda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    // Ubah tipe data List menjadi ItemPesanan (bukan ItemPesananWithProduk)
    private val cartItems: MutableList<ItemPesanan>,
    private val onIncrease: (ItemPesanan) -> Unit,
    private val onDecrease: (ItemPesanan) -> Unit,
    private val onItemSelectChanged: (ItemPesanan) -> Unit,
    private val onItemClick: (ItemPesanan) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Secondary constructor untuk checkout mode (read-only)
    constructor(cartItems: List<ItemPesanan>, isCheckout: Boolean) : this(
        cartItems.toMutableList(),
        onIncrease = {},
        onDecrease = {},
        onItemSelectChanged = {},
        onItemClick = {}
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newItems: List<ItemPesanan>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val selectCheckBox: CheckBox = itemView.findViewById(R.id.cb_select_item)
        private val productImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        private val productName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        private val quantity: TextView = itemView.findViewById(R.id.tv_quantity)
        private val decreaseButton: ImageButton = itemView.findViewById(R.id.btn_decrease_quantity)
        private val increaseButton: ImageButton = itemView.findViewById(R.id.btn_increase_quantity)

        fun bind(item: ItemPesanan) {
            // Di Firebase, ItemPesanan berdiri sendiri (flat data)

            // 1. Checkbox
            selectCheckBox.isChecked = item.isSelected

            // 2. Nama Produk (Diambil dari snapshot di ItemPesanan)
            productName.text = item.produkNama

            // 3. Harga
            val numberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            numberFormat.maximumFractionDigits = 0
            productPrice.text = numberFormat.format(item.produkHarga)

            // 4. Jumlah
            quantity.text = item.jumlah.toString()

            // 5. Gambar
            if (item.gambarUrl.isNotEmpty()) {
                productImage.load(item.gambarUrl) {
                    crossfade(true)
                    placeholder(R.color.grey)
                    error(R.drawable.ic_error_image)
                }
            } else {
                productImage.setImageResource(R.drawable.ic_error_image)
            }

            // 6. Aksi Tombol
            increaseButton.setOnClickListener { onIncrease(item) }
            decreaseButton.setOnClickListener { onDecrease(item) }

            selectCheckBox.setOnClickListener {
                // Update status checkbox, kirim objek baru ke listener
                val updatedItem = item.copy(isSelected = selectCheckBox.isChecked)
                onItemSelectChanged(updatedItem)
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
