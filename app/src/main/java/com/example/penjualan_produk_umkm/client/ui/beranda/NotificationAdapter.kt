package com.example.penjualan_produk_umkm.client.ui.beranda

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.Notification
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val notifications: List<Notification>,
    private val onNotificationClicked: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
        holder.itemView.setOnClickListener {
            onNotificationClicked(notification)
        }
    }

    override fun getItemCount() = notifications.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_notification_title)
        private val messageTextView: TextView = itemView.findViewById(R.id.tv_notification_message)
        private val timestampTextView: TextView = itemView.findViewById(R.id.tv_notification_timestamp)
        private val readStatusTextView: TextView = itemView.findViewById(R.id.tv_read_status)

        fun bind(notification: Notification) {
            titleTextView.text = notification.title
            messageTextView.text = notification.message
            timestampTextView.text = formatTimestamp(notification.timestamp)

            if (notification.readStatus) {
                titleTextView.setTypeface(null, Typeface.NORMAL)
                messageTextView.setTypeface(null, Typeface.NORMAL)
                readStatusTextView.text = "Dibaca"
            } else {
                titleTextView.setTypeface(null, Typeface.BOLD)
                messageTextView.setTypeface(null, Typeface.BOLD)
                readStatusTextView.text = "Belum Dibaca"
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
