package com.example.penjualan_produk_umkm.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminNotificationViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    val unreadCount: StateFlow<Int> = _notifications.map {
        it.count { !it.readStatus }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchAdminNotifications()
    }

    fun fetchAdminNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("notifications")
                    .whereEqualTo("recipient", "admin")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val notificationList = snapshot.documents.mapNotNull { document ->
                    document.toObject(Notification::class.java)?.copy(notificationId = document.id)
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
        val wasAlreadyRead = _notifications.value.find { it.notificationId == notificationId }?.readStatus == true
        if (wasAlreadyRead) return

        _notifications.update { currentList ->
            currentList.map {
                if (it.notificationId == notificationId) it.copy(readStatus = true) else it
            }
        }

        viewModelScope.launch {
            try {
                db.collection("notifications").document(notificationId)
                    .update("readStatus", true)
                    .await()
            } catch (e: Exception) {
                Log.e("AdminNotificationVM", "Error updating status in Firestore", e)
                 _notifications.update { currentList ->
                    currentList.map {
                        if (it.notificationId == notificationId) it.copy(readStatus = false) else it
                    }
                }
            }
        }
    }
}
