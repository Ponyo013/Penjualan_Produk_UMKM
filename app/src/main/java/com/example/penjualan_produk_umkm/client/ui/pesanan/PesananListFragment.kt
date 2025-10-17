package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.penjualan_produk_umkm.databinding.FragmentPesananListBinding
import com.example.penjualan_produk_umkm.model.StatusPesanan

class PesananListFragment : Fragment() {

    private var _binding: FragmentPesananListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PesananViewModel by viewModels({
        requireParentFragment()
    })
    private lateinit var pesananAdapter: PesananAdapter

    private var status: StatusPesanan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            status = StatusPesanan.valueOf(it.getString(ARG_STATUS) ?: "DIPROSES")
        }
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

        when (status) {
            StatusPesanan.DIPROSES -> viewModel.pesananDiproses.observe(viewLifecycleOwner) { pesananAdapter.submitList(it) }
            StatusPesanan.DIKIRIM -> viewModel.pesananDikirim.observe(viewLifecycleOwner) { pesananAdapter.submitList(it) }
            StatusPesanan.SELESAI -> viewModel.pesananSelesai.observe(viewLifecycleOwner) { pesananAdapter.submitList(it) }
            else -> { /* Do nothing */ }
        }
    }

    private fun setupRecyclerView() {
        pesananAdapter = PesananAdapter()
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
