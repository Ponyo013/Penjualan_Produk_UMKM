package com.example.penjualan_produk_umkm.owner.dashboard

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

/**
 * Handles all text preprocessing steps before feeding text to the sentiment analysis model.
 *
 * This includes cleaning, case folding, slang replacement, tokenization, stopword removal, and stemming.
 * The dictionaries for slang, stopwords, and stemming are loaded from asset files.
 *
 * @param context The application context for accessing assets.
 * @param slangFileName The name of the JSON file in assets containing the slang dictionary.
 * @param stopwordsFileName The name of the text file in assets containing the stopwords.
 * @param stemmerFileName The name of the JSON file in assets containing the stemmer dictionary.
 */
class TextPreprocessor(
    private val context: Context,
    private val slangFileName: String = "slang_indo.json",
    private val stopwordsFileName: String = "stopwords_indo.txt",
    private val stemmerFileName: String = "stemmer_dict.json"
) {

    private val slangDict: Map<String, String>
    private val stopwords: Set<String>
    private val stemmerDict: Map<String, String>

    init {
        slangDict = loadJsonDictionary(slangFileName)
        stopwords = loadStopwords(stopwordsFileName)
        stemmerDict = loadJsonDictionary(stemmerFileName)
    }

    /**
     * The main preprocessing pipeline.
     *
     * @param text The raw text to preprocess.
     * @return A list of preprocessed tokens.
     */
    fun preprocess(text: String): List<String> {
        val cleanedText = cleanText(text)
        val foldedText = caseFolding(cleanedText)
        val slangReplacedText = replaceSlang(foldedText)
        val tokens = tokenize(slangReplacedText)
        val stopwordsRemovedTokens = removeStopwords(tokens)
        return stemWords(stopwordsRemovedTokens)
    }

    // --- Private Helper Functions ---

    /**
     * Loads a dictionary from a JSON asset file.
     * @param fileName The name of the file in the assets folder.
     * @return A map representing the dictionary.
     */
    private fun <T> loadJsonDictionary(fileName: String): Map<String, T> {
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, T>>() {}.type
            Gson().fromJson(json, type) ?: emptyMap()
        } catch (e: IOException) {
            e.printStackTrace()
            // In a real app, you might want to log this error to a crash reporting tool
            emptyMap()
        }
    }

    /**
     * Loads stopwords from a text asset file.
     * @param fileName The name of the file in the assets folder.
     * @return A set of stopwords.
     */
    private fun loadStopwords(fileName: String): Set<String> {
        return try {
            context.assets.open(fileName)
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
                .map { it.trim().lowercase() }
                .toSet()
        } catch (e: IOException) {
            e.printStackTrace()
            // In a real app, you might want to log this error to a crash reporting tool
            emptySet()
        }
    }

    private fun cleanText(text: String): String {
        var cleaned = text
        cleaned = cleaned.replace(Regex("[^A-Za-z0-9\\s]"), " ") // Remove special characters
        cleaned = cleaned.replace(Regex("[0-9]+"), " ") // Remove numbers
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim() // Remove multiple spaces
        cleaned = cleaned.replace(Regex("(.)\\1{2,}"), "$1") // Remove redundant characters
        return cleaned
    }

    private fun caseFolding(text: String): String {
        return text.lowercase()
    }

    private fun replaceSlang(text: String): String {
        val words = text.split(" ")
        return words.joinToString(" ") { word ->
            slangDict[word] ?: word
        }
    }

    private fun tokenize(text: String): List<String> {
        return text.split(" ").filter { it.isNotBlank() }
    }

    private fun removeStopwords(tokens: List<String>): List<String> {
        val negationWords = setOf("tidak", "nggak", "jangan", "belum")
        return tokens.filter { token ->
            token !in stopwords || token in negationWords
        }
    }

    private fun stemWords(tokens: List<String>): List<String> {
        return tokens.map { token ->
            stemmerDict[token] ?: token
        }
    }
}
