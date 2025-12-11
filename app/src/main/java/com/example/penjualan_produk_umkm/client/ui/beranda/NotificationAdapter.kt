package com.example.penjualan_produk_umkm.client.ui.beranda

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private var notifications: List<Notification>,
    private var seenGeneralNotificationIds: Set<String>,
    private val onNotificationClicked: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        val isSeen = notification.recipient == "all_users" && seenGeneralNotificationIds.contains(notification.notificationId)
        holder.bind(notification, isSeen)
        holder.itemView.setOnClickListener {
            onNotificationClicked(notification)
        }
    }

    override fun getItemCount() = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        this.notifications = newNotifications
        notifyDataSetChanged()
    }

    fun updateSeenIds(newSeenIds: Set<String>) {
        this.seenGeneralNotificationIds = newSeenIds
        notifyDataSetChanged()
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_notification_title)
        private val messageTextView: TextView = itemView.findViewById(R.id.tv_notification_message)
        private val timestampTextView: TextView = itemView.findViewById(R.id.tv_notification_timestamp)
        private val readStatusTextView: TextView = itemView.findViewById(R.id.tv_read_status)

        fun bind(notification: Notification, isSeen: Boolean) {
            titleTextView.text = notification.title
            messageTextView.text = notification.message
            timestampTextView.text = formatTimestamp(notification.timestamp)

            val isRead = notification.readStatus || isSeen

            if (isRead) {
                titleTextView.setTypeface(null, Typeface.NORMAL)
                messageTextView.setTypeface(null, Typeface.NORMAL)
                readStatusTextView.visibility = View.GONE
            } else {
                titleTextView.setTypeface(null, Typeface.BOLD)
                messageTextView.setTypeface(null, Typeface.BOLD)
                readStatusTextView.visibility = View.VISIBLE
                readStatusTextView.text = "Baru"
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
