package com.example.penjualan_produk_umkm.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NotificationViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // --- Data Sources ---
    private val _userNotifications = MutableStateFlow<List<Notification>>(emptyList())
    private val _generalNotifications = MutableStateFlow<List<Notification>>(emptyList())

    // --- Local State for Seen General Notifications ---
    private val _seenGeneralNotificationIds = MutableStateFlow<Set<String>>(emptySet())
    val seenGeneralNotificationIds: StateFlow<Set<String>> = _seenGeneralNotificationIds.asStateFlow()

    // --- Combined and Processed Public Flows ---
    val notifications: StateFlow<List<Notification>> = 
        combine(_userNotifications, _generalNotifications) { user, general ->
            (user + general).sortedByDescending { it.timestamp }
        }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = 
        combine(notifications, _seenGeneralNotificationIds) { allNotifications, seenIds ->
            val unreadUserNotifications = allNotifications.count { it.recipient == userId && !it.readStatus }
            val unreadGeneralNotifications = allNotifications.count { it.recipient == "all_users" && it.notificationId !in seenIds }
            unreadUserNotifications + unreadGeneralNotifications
        }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    init {
        listenForNotifications()
    }

    private fun listenForNotifications() {
        if (userId == null) return

        // Listener for user-specific notifications
        db.collection("notifications")
            .whereEqualTo("recipient", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("NotificationViewModel", "User notifications listen failed.", e)
                    return@addSnapshotListener
                }
                val notificationList = snapshots?.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)?.copy(notificationId = doc.id)
                } ?: emptyList()
                _userNotifications.value = notificationList
            }

        // Listener for general notifications
        db.collection("notifications")
            .whereEqualTo("recipient", "all_users")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("NotificationViewModel", "General notifications listen failed.", e)
                    return@addSnapshotListener
                }
                val notificationList = snapshots?.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)?.copy(notificationId = doc.id)
                } ?: emptyList()
                _generalNotifications.value = notificationList
            }
    }

    fun markAsRead(notification: Notification) {
        if (notification.notificationId.isEmpty()) return

        when (notification.recipient) {
            userId -> {
                // Mark user-specific notification as read in Firestore
                if (!notification.readStatus) {
                    db.collection("notifications").document(notification.notificationId)
                        .update("readStatus", true)
                        .addOnFailureListener {
                            Log.e("NotificationViewModel", "Failed to mark notification as read", it)
                        }
                }
            }
            "all_users" -> {
                // Mark general notification as seen locally
                _seenGeneralNotificationIds.value = _seenGeneralNotificationIds.value + notification.notificationId
            }
        }
    }
}
