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
import com.example.penjualan_produk_umkm.database.relation.ItemPesananWithProduk
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartItems: MutableList<ItemPesananWithProduk>,
    private val onIncrease: (ItemPesananWithProduk) -> Unit,
    private val onDecrease: (ItemPesananWithProduk) -> Unit,
    private val onItemSelectChanged: (ItemPesananWithProduk) -> Unit,
    private val onItemClick: (ItemPesananWithProduk) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Secondary constructor untuk checkout mode (read-only)
    constructor(cartItems: List<ItemPesananWithProduk>, isCheckout: Boolean) : this(
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

    fun updateCartItems(newItems: List<ItemPesananWithProduk>) {
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

        fun bind(item: ItemPesananWithProduk) {
            val produk = item.produk
            val itemPesanan = item.itemPesanan

            // Checkbox
            selectCheckBox.isChecked = itemPesanan.isSelected

            // Nama dan harga
            productName.text = produk.nama
            productPrice.text = NumberFormat
                .getCurrencyInstance(Locale("id", "ID"))
                .format(produk.harga)

            // Jumlah
            quantity.text = itemPesanan.jumlah.toString()

            // Gambar (pakai coil)
            val firstImageId = produk.gambarResourceIds.firstOrNull()
            if (firstImageId != null) {
                productImage.load(firstImageId) {
                    placeholder(R.color.grey)
                    error(R.drawable.ic_error_image)
                    crossfade(true)
                }
            } else {
                productImage.setImageResource(R.drawable.ic_error_image)
            }

            // Aksi tombol dan checkbox
            increaseButton.setOnClickListener { onIncrease(item) }
            decreaseButton.setOnClickListener { onDecrease(item) }
            selectCheckBox.setOnClickListener {
                val updatedItem = item.copy(
                    itemPesanan = itemPesanan.copy(isSelected = selectCheckBox.isChecked)
                )
                onItemSelectChanged(updatedItem)
            }
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
