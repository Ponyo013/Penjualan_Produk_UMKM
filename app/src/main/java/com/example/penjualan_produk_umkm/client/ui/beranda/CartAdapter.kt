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
import com.example.penjualan_produk_umkm.model.ItemPesanan
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartItems: MutableList<ItemPesanan>,
    private val onIncrease: (ItemPesanan) -> Unit,
    private val onDecrease: (ItemPesanan) -> Unit,
    private val onItemSelectChanged: (ItemPesanan) -> Unit,
    private val onItemClick: (ItemPesanan) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Secondary constructor for checkout where click actions are not needed
    constructor(cartItems: List<ItemPesanan>, isCheckout: Boolean) : this(cartItems.toMutableList(), {}, {}, {}, {})

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val selectCheckBox: CheckBox = itemView.findViewById(R.id.cb_select_item)
        private val productImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        private val productName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        private val quantity: TextView = itemView.findViewById(R.id.tv_quantity)
        private val decreaseButton: ImageButton = itemView.findViewById(R.id.btn_decrease_quantity)
        private val increaseButton: ImageButton = itemView.findViewById(R.id.btn_increase_quantity)

        fun bind(item: ItemPesanan) {
            selectCheckBox.isChecked = item.isSelected
            productName.text = item.produk.nama
            val localeID = Locale.Builder().setLanguage("id").setRegion("ID").build()
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            productPrice.text = numberFormat.format(item.produk.harga)
            quantity.text = item.jumlah.toString()

            // Correctly load image from the first resource ID
            val firstImageId = item.produk.gambarResourceIds.firstOrNull()
            if (firstImageId != null) {
                productImage.load(firstImageId) {
                    crossfade(true)
                    placeholder(R.color.grey)
                }
            } else {
                // Fallback if no image is available
                productImage.setImageResource(R.drawable.ic_error_image) 
            }

            increaseButton.setOnClickListener { onIncrease(item) }
            decreaseButton.setOnClickListener { onDecrease(item) }
            selectCheckBox.setOnClickListener { onItemSelectChanged(item) }
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}