package com.example.penjualan_produk_umkm.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.penjualan_produk_umkm.database.firestore.model.Produk
import com.example.penjualan_produk_umkm.database.model.ImageKitResult
import com.google.firebase.firestore.FirebaseFirestore // Tambahkan ini
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.io.readBytes

class ProdukViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val produkCollection = db.collection("produk")
    private val pesananItemCollection = db.collection("itemPesanan")
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val storageRef = FirebaseStorage.getInstance().reference
    private val _allProduk = MutableLiveData<List<Produk>>()
    val allProduk: LiveData<List<Produk>> = _allProduk

    init {
        getAllProduk()
    }

    // 1. Ambil Data Realtime
    fun getAllProduk() {
        produkCollection.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            val list = snapshot?.documents?.mapNotNull { doc ->
                val p = doc.toObject(Produk::class.java)
                p?.id = doc.id // Set ID dokumen ke objek agar bisa di-edit/delete
                p
            } ?: emptyList()

            _allProduk.value = list
        }
    }

    // 2. Tambah Produk
    fun insertProduk(
        produk: Produk,
        uri: Uri?,
        context: Context,
        onComplete: (Boolean, String?) -> Unit
    ) {
        _isLoading.value = true

        if (uri != null) {
            // Upload gambar dulu ke ImageKit
            uploadImageToImageKit(uri, context) { imageKitResult ->
                if (imageKitResult != null) {
                    // Set URL & fileId ke produk
                    produk.gambarUrl = imageKitResult.url
                    produk.imageKitFileId = imageKitResult.fileId

                    // Simpan ke Firestore
                    saveProdukFirestore(produk, onComplete)
                } else {
                    _isLoading.value = false
                    onComplete(false, "Gagal upload gambar ke ImageKit")
                }
            }
        } else {
            // Kalau tidak ada gambar, langsung simpan
            saveProdukFirestore(produk, onComplete)
        }
    }

    // Helper untuk menyimpan produk ke Firestore
    private fun saveProdukFirestore(
        produk: Produk,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val newDoc = produkCollection.document()
        produk.id = newDoc.id
        newDoc.set(produk)
            .addOnSuccessListener {
                _isLoading.value = false
                onComplete(true, null)
            }
            .addOnFailureListener {
                _isLoading.value = false
                onComplete(false, "Gagal menyimpan produk ke Firestore")
            }
    }


    // 3. Update Produk
    fun updateProduk(
        produk: Produk,
        newGambarUri: Uri?, // Jika null, gambar tidak diganti
        context: Context,
        onComplete: (Boolean, String?) -> Unit
    ) {
        _isLoading.value = true

        if (newGambarUri != null) {
            // Upload gambar baru ke ImageKit
            uploadImageToImageKit(newGambarUri, context) { imageKitResult ->
                if (imageKitResult != null) {
                    // Hapus gambar lama dari ImageKit jika ada
                    val oldFileId = produk.imageKitFileId
                    if (oldFileId.isNotEmpty()) {
                        deleteImageKitFile(oldFileId) { success ->
                            if (!success) {
                                Log.w("UPDATE_PRODUK", "Gagal menghapus gambar lama di ImageKit")
                            }
                        }
                    }

                    // Update produk dengan URL & fileId baru
                    produk.gambarUrl = imageKitResult.url
                    produk.imageKitFileId = imageKitResult.fileId

                    // Update Firestore
                    produkCollection.document(produk.id)
                        .set(produk)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            onComplete(true, null)
                        }
                        .addOnFailureListener { e ->
                            _isLoading.value = false
                            onComplete(false, e.message)
                        }
                } else {
                    _isLoading.value = false
                    onComplete(false, "Gagal upload gambar baru ke ImageKit")
                }
            }
        } else {
            // Jika tidak ada gambar baru, cukup update produk di Firestore
            produkCollection.document(produk.id)
                .set(produk)
                .addOnSuccessListener {
                    _isLoading.value = false
                    onComplete(true, null)
                }
                .addOnFailureListener { e ->
                    _isLoading.value = false
                    onComplete(false, e.message)
                }
        }
    }

    // 4. Hapus Produk + Item Keranjang
    fun deleteProduk(produk: Produk) {
        val produkId = produk.id
        if (produkId.isEmpty()) return

        // 1. Hapus gambar dari ImageKit
        if (produk.imageKitFileId.isNotEmpty()) {
            deleteImageKitFile(produk.imageKitFileId) { success ->
                Log.d("ProdukViewModel", "Hapus ImageKit: $success")
            }
        }

        // 2. Hapus produk dari Firestore
        produkCollection.document(produkId).delete()
            .addOnSuccessListener {
                // Jika ada item keranjang, hapus juga
                deleteProdukFromCart(produkId)
            }
    }


    // Menghapus foto dari Image Kit
    fun deleteImageKitFile(fileId: String, onComplete: (Boolean) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.imagekit.io/v1/files/$fileId")
            .delete()
            .addHeader("Authorization", Credentials.basic("private_lV81GYgjhiMyHcvWIJIK3z0R2vo=", ""))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onComplete(false)
            }

            override fun onResponse(call: Call, response: Response) {
                onComplete(response.isSuccessful)
            }
        })
    }


    // Fungsi untuk menghapus produk dari semua keranjang
    private fun deleteProdukFromCart(produkId: String) {
        pesananItemCollection
            .whereEqualTo("produkId", produkId)
            .whereEqualTo("status", "KERANJANG") // hanya menghapus produk di keranjang
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot == null) return@addSnapshotListener

                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
            }
    }

    // menyimpan image di ImageKit
    fun uploadImageToImageKit(
        uri: Uri,
        context: Context,
        onResult: (ImageKitResult?) -> Unit
    ){
        val client = OkHttpClient()

        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            onResult(null)
            return
        }

        val bytes = inputStream.readBytes()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg",
                bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
            .addFormDataPart("fileName", "produk_${System.currentTimeMillis()}.jpg")
            .addFormDataPart("folder", "/produk")
            .build()

        val request = Request.Builder()
            .url("https://upload.imagekit.io/api/v1/files/upload")
            .addHeader("Authorization", Credentials.basic("private_lV81GYgjhiMyHcvWIJIK3z0R2vo=", ""))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val url = json.optString("url")
                    val fileId = json.optString("fileId")
                    if (url.isNotEmpty() && fileId.isNotEmpty()) {
                        onResult(ImageKitResult(url, fileId))
                    } else {
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }
            }
        })
    }


    // 5. Get by ID (Untuk Edit/Detail)
    fun getProdukById(id: String, onResult: (Produk?) -> Unit) {
        produkCollection.document(id).get()
            .addOnSuccessListener { document ->
                val p = document.toObject(Produk::class.java)
                p?.id = document.id
                onResult(p)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }


}