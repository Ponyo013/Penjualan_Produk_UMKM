package com.example.penjualan_produk_umkm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.penjualan_produk_umkm.database.dao.EkspedisiDao
import com.example.penjualan_produk_umkm.database.model.Ekspedisi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EkspedisiViewModel(private val dao: EkspedisiDao) : ViewModel() {

    private val _list = MutableLiveData<List<Ekspedisi>>()
    val list: LiveData<List<Ekspedisi>> get() = _list

    private val _addState = MutableLiveData<Boolean>()
    val addState: LiveData<Boolean> get() = _addState

    private val _updateState = MutableLiveData<Boolean>()
    val updateState: LiveData<Boolean> get() = _updateState

    fun load() {
        viewModelScope.launch {
            _list.value = withContext(Dispatchers.IO) {
                dao.getAll()
            }
        }
    }

    fun add(ekspedisi: Ekspedisi) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.insert(ekspedisi)
            }
            _addState.value = true
            load()
        }
    }

    fun update(ekspedisi: Ekspedisi) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.update(ekspedisi)
            }
            _updateState.value = true
            load()
        }
    }

    fun delete(ekspedisi: Ekspedisi) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.delete(ekspedisi)
            }
            load()
        }
    }
}
