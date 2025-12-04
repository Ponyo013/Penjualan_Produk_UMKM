package com.example.penjualan_produk_umkm.owner.dashboard

// File: SentimentAnalyzer.kt

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class SentimentAnalyzer(private val context: Context) {

    // Member Variables untuk Model
    private lateinit var interpreter: Interpreter
    private lateinit var vocabulary: Map<String, Int>
    private lateinit var idfValues: FloatArray
    private lateinit var labelMapping: Map<Int, String>
    private var vocabSize: Int = 0

    // Deklarasi member variable untuk TextPreprocessor (BARU/DIPERBAIKI)
    private val textPreprocessor: TextPreprocessor

    // Blok init utama untuk memuat semua sumber daya
    init {
        // 1. Inisialisasi TextPreprocessor HANYA SEKALI
        // Semua kamus preprocessing dimuat di sini.
        textPreprocessor = TextPreprocessor(context)

        // 2. Muat sumber daya Model
        loadModel()
        loadVocabulary()
        loadIdfValues()
        loadLabelMapping()
    }

    // --- Fungsi Pemuatan Sumber Daya ---

    private fun loadModel() {
        val modelFile = loadModelFile("sentiment_model.tflite")
        interpreter = Interpreter(modelFile)
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadVocabulary() {
        val json = context.assets.open("vocabulary.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)
        vocabulary = mutableMapOf<String, Int>().apply {
            jsonObject.keys().forEach { key ->
                put(key, jsonObject.getInt(key))
            }
        }
        vocabSize = vocabulary.size
    }

    private fun loadIdfValues() {
        val json = context.assets.open("idf_values.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)
        idfValues = FloatArray(jsonArray.length()) { i ->
            jsonArray.getDouble(i).toFloat()
        }
    }

    private fun loadLabelMapping() {
        val json = context.assets.open("label_mapping.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)
        labelMapping = mutableMapOf<Int, String>().apply {
            jsonObject.keys().forEach { key ->
                put(key.toInt(), jsonObject.getString(key))
            }
        }
    }

    // --- Fungsi Preprocessing dan Vektorisasi ---

    /**
     * Preprocess text - menggunakan TextPreprocessor yang sudah diinisialisasi
     */
    private fun preprocessText(text: String): List<String> {
        // HANYA memanggil fungsi preprocess dari instance yang sudah dimuat di init
        return textPreprocessor.preprocess(text)
    }

    /**
     * Convert tokens to TF-IDF vector
     */
    private fun tokensToTfidf(tokens: List<String>): FloatArray {

        val vector = FloatArray(vocabSize)

        // Calculate term frequency
        val termFreq = mutableMapOf<String, Int>()
        tokens.forEach { token ->
            termFreq[token] = (termFreq[token] ?: 0) + 1
        }

        // Calculate TF-IDF
        val docLength = tokens.size.toFloat()
        termFreq.forEach { (term, count) ->
            vocabulary[term]?.let { index ->
                val tf = count / docLength
                val idf = idfValues[index]
                vector[index] = tf * idf
            }
        }

        // Normalize (L2 norm)
        val norm = sqrt(vector.map { it * it }.sum())
        if (norm > 0) {
            for (i in vector.indices) {
                vector[i] /= norm
            }
        }

        return vector
    }

    // --- Fungsi Prediksi ---

    /**
     * Predict sentiment from text
     */
    fun predict(text: String): SentimentResult {
        // Preprocess
        val tokens = preprocessText(text) // Memanggil fungsi yang sudah diperbaiki

        // Convert to TF-IDF
        val tfidfVector = tokensToTfidf(tokens)

        // Prepare input
        val inputArray = Array(1) { FloatArray(vocabSize) }
        inputArray[0] = tfidfVector

        // Prepare output
        val outputArray = Array(1) { FloatArray(labelMapping.size) }

        // Run inference
        interpreter.run(inputArray, outputArray)

        // Get prediction
        val probabilities = outputArray[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val confidence = probabilities[maxIndex]
        val label = labelMapping[maxIndex] ?: "Unknown"

        return SentimentResult(
            label = label,
            confidence = confidence,
            probabilities = probabilities.mapIndexed { index, prob ->
                labelMapping[index]!! to prob
            }.toMap()
        )
    }

    /**
     * Close the interpreter
     */
    fun close() {
        interpreter.close()
    }
}

/**
 * Result data class
 */
data class SentimentResult(
    val label: String,
    val confidence: Float,
    val probabilities: Map<String, Float>
)

/**
 * Usage example:
 *
 * val analyzer = SentimentAnalyzer(context)
 * val result = analyzer.predict("Aplikasi ini sangat bagus dan membantu!")
 * println("Sentiment: ${result.label}")
 * println("Confidence: ${result.confidence}")
 * analyzer.close()
 */