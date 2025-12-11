package com.example.penjualan_produk_umkm.client.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan_produk_umkm.R
import com.example.penjualan_produk_umkm.viewModel.NotificationViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var notificationAdapter: NotificationAdapter
    private val viewModel: NotificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_notifications)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()
        setupToolbar(view)
        observeViewModel()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(emptyList(), emptySet()) { notification ->
            viewModel.markAsRead(notification)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe all notifications
                launch {
                    viewModel.notifications.collectLatest { notifications ->
                        progressBar.visibility = View.GONE
                        notificationAdapter.updateNotifications(notifications)
                    }
                }

                // Observe seen general notifications
                launch {
                    viewModel.seenGeneralNotificationIds.collectLatest { seenIds ->
                        notificationAdapter.updateSeenIds(seenIds)
                    }
                }
            }
        }
    }
}
