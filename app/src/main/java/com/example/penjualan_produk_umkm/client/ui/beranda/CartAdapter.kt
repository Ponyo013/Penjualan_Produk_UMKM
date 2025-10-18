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
    private val isCheckout: Boolean = false,
    private val onIncrease: ((ItemPesanan) -> Unit)? = null,
    private val onDecrease: ((ItemPesanan) -> Unit)? = null,
    private val onItemSelectChanged: ((ItemPesanan) -> Unit)? = null
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Constructor for CartFragment
    constructor(
        cartItems: MutableList<ItemPesanan>,
        onIncrease: (ItemPesanan) -> Unit,
        onDecrease: (ItemPesanan) -> Unit,
        onItemSelectChanged: (ItemPesanan) -> Unit
    ) : this(cartItems, false, onIncrease, onDecrease, onItemSelectChanged)

    // Constructor for CheckoutFragment
    constructor(
        cartItems: List<ItemPesanan>,
        isCheckout: Boolean
    ) : this(cartItems.toMutableList(), isCheckout)


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
            // Common properties
            productName.text = item.produk.nama
            val localeID = Locale.Builder().setLanguage("id").setRegion("ID").build()
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            productPrice.text = numberFormat.format(item.produk.harga)
            if (item.produk.gambarResourceIds.isNotEmpty()) {
                productImage.load(item.produk.gambarResourceIds.first()) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background)
                }
            } else {
                productImage.setImageResource(R.drawable.ic_launcher_background)
            }

            if (isCheckout) {
                // Checkout mode
                selectCheckBox.visibility = View.GONE
                decreaseButton.visibility = View.GONE
                increaseButton.visibility = View.GONE
                quantity.visibility = View.VISIBLE
                quantity.text = "x${item.jumlah}"
            } else {
                // Cart mode
                selectCheckBox.visibility = View.VISIBLE
                selectCheckBox.isChecked = item.isSelected
                decreaseButton.visibility = View.VISIBLE
                increaseButton.visibility = View.VISIBLE
                quantity.visibility = View.VISIBLE
                quantity.text = item.jumlah.toString()

                // Set listeners for cart actions
                selectCheckBox.setOnClickListener {
                    onItemSelectChanged?.invoke(item)
                }
                increaseButton.setOnClickListener {
                    onIncrease?.invoke(item)
                }
                decreaseButton.setOnClickListener {
                    onDecrease?.invoke(item)
                }
            }
        }
    }
}
