package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.database.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "NotificationFragment"

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var notificationAdapter: NotificationAdapter
    private val notifications = mutableListOf<Notification>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.rv_notifications)
        progressBar = view.findViewById(R.id.progressBar)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        notificationAdapter = NotificationAdapter(notifications) { notification ->
            if (!notification.readStatus) {
                updateReadStatus(notification)
            }
        }
        recyclerView.adapter = notificationAdapter

        // Navigate Back
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        fetchNotifications()
    }

    private fun fetchNotifications() {
        progressBar.visibility = View.VISIBLE
        db.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                progressBar.visibility = View.GONE
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    notifications.clear()
                    for (document in snapshots) {
                        val notification = document.toObject(Notification::class.java).copy(id = document.id)
                        notifications.add(notification)
                    }
                    notificationAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun updateReadStatus(notification: Notification) {
        db.collection("notifications").document(notification.id)
            .update("readStatus", true)
            .addOnSuccessListener {
                Log.d(TAG, "Notification read status updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating notification read status.", e)
            }
    }
}
