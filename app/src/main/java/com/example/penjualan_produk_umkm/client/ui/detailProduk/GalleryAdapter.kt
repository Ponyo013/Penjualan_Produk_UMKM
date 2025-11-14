package com.example.penjualan_produk_umkm.client.ui.detailProduk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.penjualan_produk_umkm.R

/**
 * Adapter untuk gallery produk.
 * Bisa menampilkan gambar dari resource ID atau URL.
 * Mendukung klik untuk preview/fullscreen.
 */
class GalleryAdapter(
    private val images: List<String>, // String bisa URL atau resourceId.toString()
    private val onImageClick: ((position: Int) -> Unit)? = null
) : RecyclerView.Adapter<GalleryAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.gallery_image_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        val resourceId = image.toIntOrNull()
        if (resourceId != null) {
            holder.imageView.load(resourceId) {
                placeholder(R.color.grey)
                crossfade(true)
            }
        } else {
            holder.imageView.load(image) {
                placeholder(R.color.grey)
                crossfade(true)
            }
        }

        holder.imageView.setOnClickListener {
            onImageClick?.invoke(position)
        }
    }

    override fun getItemCount(): Int = images.size
}
