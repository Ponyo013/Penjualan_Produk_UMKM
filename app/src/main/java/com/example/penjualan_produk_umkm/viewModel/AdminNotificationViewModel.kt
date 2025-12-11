package com.example.penjualan_produk_umkm.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminNotificationViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchAdminNotifications()
    }

    private fun fetchAdminNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("notifications")
                    .whereEqualTo("recipient", "admin")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val notificationList = snapshot.documents.mapNotNull { document ->
                    document.toObject(Notification::class.java)?.copy(id = document.id)
                }
                _notifications.value = notificationList
            } catch (e: Exception) {
                Log.e("AdminNotificationVM", "Error fetching notifications", e)
                _error.value = "Gagal memuat notifikasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                db.collection("notifications").document(notificationId)
                    .update("readStatus", true)
                    .await()
                // Refresh the list to show the change
                fetchAdminNotifications()
            } catch (e: Exception) {
                Log.e("AdminNotificationVM", "Error updating notification status", e)
                _error.value = "Gagal memperbarui status notifikasi: ${e.message}"
            }
        }
    }
}
