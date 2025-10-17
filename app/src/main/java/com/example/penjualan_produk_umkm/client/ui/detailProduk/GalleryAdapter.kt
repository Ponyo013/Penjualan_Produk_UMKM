// File: com/example/penjualan_produk_umkm/client/ui/detailProduk/GalleryAdapter.kt

package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import coil.load

class GalleryAdapter(private val imageUrls: List<Int>) :
    RecyclerView.Adapter<GalleryAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.gallery_image_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false) // <-- Layout item gambar baru
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.load(imageUrls[position]) {
            placeholder(R.color.grey)
            crossfade(true)
        }
    }

    override fun getItemCount() = imageUrls.size
}

// **CATATAN:** Anda perlu membuat layout: res/layout/item_gallery_image.xml
// Yang hanya berisi satu ImageView dengan ID: gallery_image_item