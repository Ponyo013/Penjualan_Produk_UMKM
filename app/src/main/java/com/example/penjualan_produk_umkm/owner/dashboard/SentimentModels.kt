package com.example.penjualan_produk_umkm.owner.dashboard

data class Review(
    val id: String = "",
    val komentar: String = "",
    val produkId: String = "",
    val rating: Float = 0f,
    val tanggal: Long = 0L,
    val userId: String = "",
    val userName: String = "",
    val sentiment: String = "Netral",
    val sentimentConfidence: Float = 0.0f
)

// Data class untuk statistik sentimen
data class SentimentStats(
    val positif: Int = 0,
    val negatif: Int = 0,
    val netral: Int = 0,
    val total: Int = 0
) {
    val positifPercent: Float get() = if (total > 0) (positif.toFloat() / total * 100) else 0f
    val negatifPercent: Float get() = if (total > 0) (negatif.toFloat() / total * 100) else 0f
    val netralPercent: Float get() = if (total > 0) (netral.toFloat() / total * 100) else 0f
}

// Data class untuk hasil analisis sentimen
data class SentimentResult(
    val label: String,
    val confidence: Float,
    val probabilities: Map<String, Float>
)