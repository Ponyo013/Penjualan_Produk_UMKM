package com.example.penjualan_produk_umkm.ml

import android.content.Context
import com.example.penjualan_produk_umkm.owner.dashboard.SentimentResult
import com.example.penjualan_produk_umkm.owner.dashboard.TextPreprocessor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

/**
 * Performs sentiment analysis on text using a TensorFlow Lite model.
 *
 * This class handles loading the model and required assets, preprocessing text,
 * and running inference to predict the sentiment.
 *
 * @param context The application context for accessing assets.
 * @param textPreprocessor The text preprocessor to use.
 * @param modelFileName The name of the TFLite model file in assets.
 * @param vocabularyFileName The name of the JSON file in assets for the vocabulary.
 * @param idfFileName The name of the JSON file in assets for IDF values.
 * @param labelMappingFileName The name of the JSON file in assets for the label mapping.
 */
class SentimentAnalyzer(
    private val context: Context,
    private val textPreprocessor: TextPreprocessor,
    private val modelFileName: String = "sentiment_model.tflite",
    private val vocabularyFileName: String = "vocabulary.json",
    private val idfFileName: String = "idf_values.json",
    private val labelMappingFileName: String = "label_mapping.json"
) {

    private lateinit var interpreter: Interpreter
    private lateinit var vocabulary: Map<String, Int>
    private lateinit var idfValues: FloatArray
    private lateinit var labelMapping: Map<Int, String>
    private var vocabSize: Int = 0

    init {
        loadModel()
        loadVocabulary()
        loadIdfValues()
        loadLabelMapping()
    }

    /**
     * Predicts the sentiment of a given text.
     *
     * @param text The text to analyze.
     * @return A [SentimentResult] containing the prediction.
     */
    fun predict(text: String): SentimentResult {
        val tokens = textPreprocessor.preprocess(text)
        val tfidfVector = tokensToTfidf(tokens)

        val inputArray = arrayOf(tfidfVector)
        val outputArray = Array(1) { FloatArray(labelMapping.size) }

        interpreter.run(inputArray, outputArray)

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
     * Closes the TFLite interpreter.
     */
    fun close() {
        interpreter.close()
    }

    // --- Private Helper Functions ---

    private fun loadModel() {
        try {
            val modelFile = loadModelFile(modelFileName)
            interpreter = Interpreter(modelFile)
        } catch (e: IOException) {
            e.printStackTrace()
            // In a real app, you might want to log this error to a crash reporting tool
        }
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
        vocabulary = loadJsonAsset(vocabularyFileName) ?: emptyMap()
        vocabSize = vocabulary.size
    }

    private fun loadIdfValues() {
        val idfList: List<Double>? = loadJsonAsset(idfFileName)
        idfValues = idfList?.map { it.toFloat() }?.toFloatArray() ?: floatArrayOf()
    }

    private fun loadLabelMapping() {
        val stringMap: Map<String, String>? = loadJsonAsset(labelMappingFileName)
        labelMapping = stringMap?.mapKeys { it.key.toInt() } ?: emptyMap()
    }

    private inline fun <reified T> loadJsonAsset(fileName: String): T? {
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<T>() {}.type
            Gson().fromJson(json, type)
        } catch (e: IOException) {
            e.printStackTrace()
            // In a real app, you might want to log this error to a crash reporting tool
            null
        }
    }

    private fun tokensToTfidf(tokens: List<String>): FloatArray {
        val vector = FloatArray(vocabSize)
        val termFreq = calculateTermFrequency(tokens)

        termFreq.forEach { (term, count) ->
            vocabulary[term]?.let { index ->
                if (index < idfValues.size) {
                    val tf = count / tokens.size.toFloat()
                    val idf = idfValues[index]
                    vector[index] = tf * idf
                }
            }
        }

        return normalize(vector)
    }

    private fun calculateTermFrequency(tokens: List<String>): Map<String, Int> {
        return tokens.groupingBy { it }.eachCount()
    }

    private fun normalize(vector: FloatArray): FloatArray {
        val norm = sqrt(vector.sumOf { (it * it).toDouble() }).toFloat()
        if (norm > 0) {
            for (i in vector.indices) {
                vector[i] /= norm
            }
        }
        return vector
    }
}
