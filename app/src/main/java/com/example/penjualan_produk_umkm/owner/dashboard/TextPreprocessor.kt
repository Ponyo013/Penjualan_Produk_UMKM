package com.example.penjualan_produk_umkm.owner.dashboard

import android.content.Context
import org.json.JSONObject

class TextPreprocessor(private val context: Context) {

    private lateinit var slangDict: Map<String, String>
    private lateinit var stopwords: Set<String>
    private lateinit var stemmerDict: Map<String, String>

    init {
        loadSlangDictionary()
        loadStopwords()
        loadStemmerDictionary()
    }

    private fun loadSlangDictionary() {
        // Load slang dictionary from JSON
        val json = context.assets.open("slang_indo.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)
        slangDict = mutableMapOf<String, String>().apply {
            jsonObject.keys().forEach { key ->
                put(key, jsonObject.getString(key))
            }
        }
    }

    private fun loadStopwords() {
        // Load stopwords from text file
        stopwords = context.assets.open("stopwords_indo.txt")
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }
            .map { it.trim().toLowerCase() }
            .toSet()
    }

    private fun loadStemmerDictionary() {
        // Load pre-computed stemmer dictionary
        val json = context.assets.open("stemmer_dict.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)
        stemmerDict = mutableMapOf<String, String>().apply {
            jsonObject.keys().forEach { key ->
                put(key, jsonObject.getString(key))
            }
        }
    }

    /**
     * Clean text: remove special characters and numbers
     */
    private fun cleanText(text: String): String {
        var cleaned = text

        // Remove special characters
        cleaned = cleaned.replace(Regex("[^A-Za-z0-9\\s]"), " ")

        // Remove numbers
        cleaned = cleaned.replace(Regex("[0-9]+"), " ")

        // Remove multiple spaces
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()

        // Remove redundant characters (more than 2 consecutive)
        cleaned = cleaned.replace(Regex("(.)\\1{2,}"), "$1")

        return cleaned
    }

    /**
     * Convert to lowercase
     */
    private fun caseFolding(text: String): String {
        return text.toLowerCase()
    }

    /**
     * Replace slang words with formal words
     */
    private fun replaceSlang(text: String): String {
        val words = text.split(" ")
        return words.joinToString(" ") { word ->
            slangDict[word] ?: word
        }
    }

    /**
     * Tokenize text
     */
    private fun tokenize(text: String): List<String> {
        return text.split(" ").filter { it.isNotBlank() }
    }

    /**
     * Remove stopwords (except negation words)
     */
    private fun removeStopwords(tokens: List<String>): List<String> {
        // Keep negation words
        val negationWords = setOf("tidak", "nggak", "jangan", "belum")
        return tokens.filter { token ->
            token !in stopwords || token in negationWords
        }
    }

    /**
     * Stem words to their root form
     */
    private fun stemWords(tokens: List<String>): List<String> {
        return tokens.map { token ->
            stemmerDict[token] ?: token
        }
    }

    /**
     * Complete preprocessing pipeline
     */
    fun preprocess(text: String): List<String> {
        // Step 1: Clean text
        var processed = cleanText(text)

        // Step 2: Case folding
        processed = caseFolding(processed)

        // Step 3: Replace slang
        processed = replaceSlang(processed)

        // Step 4: Tokenize
        var tokens = tokenize(processed)

        // Step 5: Remove stopwords
        tokens = removeStopwords(tokens)

        // Step 6: Stemming
        tokens = stemWords(tokens)

        return tokens
    }
}

/**
 * Run this Python script to create stemmer_dict.json
 * from your existing stemming results
 */