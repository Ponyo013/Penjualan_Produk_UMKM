package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.penjualan_produk_umkm.ViewModelFactory
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.firestore.model.StatusPesanan
import com.example.penjualan_produk_umkm.databinding.FragmentPesananListBinding
import com.example.penjualan_produk_umkm.viewModel.PesananViewModel

class PesananListFragment : Fragment() {

    private var _binding: FragmentPesananListBinding? = null
    private val binding get() = _binding!!

    private lateinit var pesananAdapter: PesananAdapter
    private var status: StatusPesanan? = null

    // FIX: Gunakan 'by viewModels' dengan Factory kosong
    // ViewModel akan handle auth dan database sendiri
    private val viewModel: PesananViewModel by viewModels {
        ViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Ambil status dari argument string
            val statusName = it.getString(ARG_STATUS) ?: "DIPROSES"
            status = try {
                StatusPesanan.valueOf(statusName)
            } catch (e: Exception) {
                StatusPesanan.DIPROSES
            }
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

        // Observe LiveData sesuai status pesanan
        when (status) {
            StatusPesanan.DIPROSES -> viewModel.pesananDiproses.observe(viewLifecycleOwner) { list ->
                pesananAdapter.submitList(list.sortedByDescending { it.tanggal })
            }
            StatusPesanan.DIKIRIM -> viewModel.pesananDikirim.observe(viewLifecycleOwner) { list ->
                pesananAdapter.submitList(list.sortedByDescending { it.tanggal })
            }
            StatusPesanan.SELESAI -> viewModel.pesananSelesai.observe(viewLifecycleOwner) { list ->
                pesananAdapter.submitList(list.sortedByDescending { it.tanggal })
            }
            StatusPesanan.DIBATALKAN -> viewModel.pesananDibatalkan.observe(viewLifecycleOwner) { list ->
                pesananAdapter.submitList(list.sortedByDescending { it.tanggal })
            }
            else -> { /* Status lain (misal Keranjang) tidak ditampilkan di sini */ }
        }
    }

    private fun setupRecyclerView() {
        // Kirim viewModel dan lifecycleOwner ke adapter (untuk observasi sub-item)
        pesananAdapter = PesananAdapter(viewModel, viewLifecycleOwner, onCancelClick = { pesanan ->
            showCancellationDialog(pesanan)
        })

        binding.rvPesananList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pesananAdapter
        }
    }

    private fun showCancellationDialog(pesanan: Pesanan) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Batalkan Pesanan?")
            .setMessage("Pesanan akan dibatalkan. Apakah Anda yakin?")
            .setPositiveButton("Ya, Batalkan") { dialog, _ ->
                // Panggil ViewModel
                viewModel.cancelPesanan(
                    pesanan,
                    onSuccess = {
                        android.widget.Toast.makeText(context, "Pesanan berhasil dibatalkan", android.widget.Toast.LENGTH_SHORT).show()
                        // Tidak perlu refresh manual, karena pakai SnapshotListener (Realtime)
                    },
                    onError = { msg ->
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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