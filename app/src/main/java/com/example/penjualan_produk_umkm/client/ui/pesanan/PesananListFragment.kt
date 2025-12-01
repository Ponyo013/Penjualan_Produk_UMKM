package com.example.penjualan_produk_umkm.client.ui.pesanan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.penjualan_produk_umkm.database.firestore.model.Pesanan
import com.example.penjualan_produk_umkm.database.model.StatusPesanan
import com.example.penjualan_produk_umkm.databinding.FragmentPesananListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PesananListFragment : Fragment() {

    private var _binding: FragmentPesananListBinding? = null
    private val binding get() = _binding!!

    private lateinit var pesananAdapter: PesananAdapter
    private var status: StatusPesanan? = null

    private val db = FirebaseFirestore.getInstance()
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // StateFlow untuk pesanan
    private val _pesananFlow = MutableStateFlow<List<Pesanan>>(emptyList())
    private val pesananFlow: StateFlow<List<Pesanan>> get() = _pesananFlow

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
        observePesanan()
    }

    private fun setupRecyclerView() {
        pesananAdapter = PesananAdapter(viewLifecycleOwner)
        binding.rvPesananList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pesananAdapter
        }
    }

    private fun observePesanan() {
        db.collection("pesanan")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status?.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val pesananList = snapshot?.toObjects(Pesanan::class.java) ?: emptyList()
                // Untuk setiap pesanan, ambil itemPesanan
                lifecycleScope.launch {
                    val itemsMap = mutableMapOf<String, List<com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan>>()
                    pesananList.forEach { pesanan ->
                        val itemsSnapshot = db.collection("itemPesanan")
                            .whereEqualTo("pesananId", pesanan.id)
                            .get()
                            .addOnSuccessListener { query ->
                                val items = query.toObjects(com.example.penjualan_produk_umkm.database.firestore.model.ItemPesanan::class.java)
                                itemsMap[pesanan.id] = items
                                pesananAdapter.submitList(pesananList, itemsMap)
                            }
                    }
                }
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
