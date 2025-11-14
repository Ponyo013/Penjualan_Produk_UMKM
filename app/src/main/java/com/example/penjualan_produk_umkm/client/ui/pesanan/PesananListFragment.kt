package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.AppDatabase
import com.example.penjualan_produk_umkm.database.model.StatusPesanan
import com.example.penjualan_produk_umkm.databinding.FragmentPesananListBinding
import com.example.penjualan_produk_umkm.viewModel.PesananViewModel

class PesananListFragment : Fragment() {

    private var _binding: FragmentPesananListBinding? = null
    private val binding get() = _binding!!

    private lateinit var pesananAdapter: PesananAdapter
    private var status: StatusPesanan? = null

    private lateinit var viewModel: PesananViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            status = StatusPesanan.valueOf(it.getString(ARG_STATUS) ?: "DIPROSES")
        }

        // Inisialisasi database dan ViewModel
        val db = AppDatabase.getDatabase(requireContext())
        val prefs = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE)
        val currentUserId = prefs.getInt("userId", 0)

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(db = db, userId = currentUserId)
        ).get(PesananViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPesananListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Observe LiveData sesuai status pesanan
        when (status) {
            StatusPesanan.DIPROSES -> viewModel.pesananDiproses.observe(viewLifecycleOwner) {
                pesananAdapter.submitList(it)
            }
            StatusPesanan.DIKIRIM -> viewModel.pesananDikirim.observe(viewLifecycleOwner) {
                pesananAdapter.submitList(it)
            }
            StatusPesanan.SELESAI -> viewModel.pesananSelesai.observe(viewLifecycleOwner) {
                pesananAdapter.submitList(it)
            }
            StatusPesanan.DIBATALKAN -> viewModel.pesananDibatalkan.observe(viewLifecycleOwner) {
                pesananAdapter.submitList(it)
            }
            else -> { /* do nothing */ }
        }
    }

    private fun getCurrentUserId(): Int {
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("USER_ID", 0)
    }

    private fun setupRecyclerView() {
        pesananAdapter = PesananAdapter(viewModel, viewLifecycleOwner)
        binding.rvPesananList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pesananAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_STATUS = "status"

        @JvmStatic
        fun newInstance(status: StatusPesanan) =
            PesananListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STATUS, status.name)
                }
            }
    }
}
