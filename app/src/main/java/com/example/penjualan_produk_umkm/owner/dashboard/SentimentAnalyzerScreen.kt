package com.example.penjualan_produk_umkm.owner.dashboard // Adjust package as needed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.penjualan_produk_umkm.ml.SentimentAnalyzer
import com.example.penjualan_produk_umkm.owner.dashboard.TextPreprocessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Assuming SentimentAnalyzer, and SentimentResult are accessible
// If they are in a different package, add the necessary import statements here.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentimentAnalyzerScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // States for UI
    var inputText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("Results will appear here...") }
    var isAnalyzing by remember { mutableStateOf(false) }

    // States for Model/Preprocessor
    var analyzer: SentimentAnalyzer? by remember { mutableStateOf(null) }
    var modelStatus by remember { mutableStateOf("Initializing model...") }

    // Initialization Logic (Runs once when the screen is composed)
    DisposableEffect(Unit) {
        try {
            val textPreprocessor = TextPreprocessor(context)
            analyzer = SentimentAnalyzer(context, textPreprocessor)
            modelStatus = "Model loaded successfully! Ready to analyze."
        } catch (e: Exception) {
            modelStatus = "Error loading model: ${e.message}"
            e.printStackTrace()
            analyzer?.close()
        }

        // Cleanup on disposal
        onDispose {
            analyzer?.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sentiment Analyzer") },
                // Add navigation icon if needed
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Model Status ---
            Text(
                text = modelStatus,
                color = if (modelStatus.startsWith("Error")) Color.Red else Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // --- Input Field ---
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter text to analyze...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                enabled = !isAnalyzing && analyzer != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Analyze Button ---
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        analyzeSentiment(
                            text = inputText,
                            analyzer = analyzer,
                            onStart = {
                                isAnalyzing = true
                                resultText = "Analyzing..."
                            },
                            onResult = { result ->
                                resultText = formatResult(result)
                                isAnalyzing = false
                            },
                            onError = { e ->
                                resultText = "Error: ${e.message}"
                                isAnalyzing = false
                            },
                            scope = coroutineScope
                        )
                    } else {
                        resultText = "Please enter some text to analyze"
                    }
                },
                enabled = !isAnalyzing && analyzer != null && inputText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(if (isAnalyzing) "Analyzing..." else "Analyze Sentiment")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Result Display ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)) // Light background
            ) {
                Text(
                    text = resultText,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// --- Supporting Functions ---

// Runs the analysis in a background coroutine
private fun analyzeSentiment(
    text: String,
    analyzer: SentimentAnalyzer?,
    onStart: () -> Unit,
    onResult: (SentimentResult) -> Unit,
    onError: (Exception) -> Unit,
    scope: CoroutineScope
) {
    if (analyzer == null) {
        onError(IllegalStateException("Model is not initialized."))
        return
    }

    onStart()
    scope.launch(Dispatchers.Default) {
        try {
            val result = analyzer.predict(text)

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e)
            }
            e.printStackTrace()
        }
    }
}

// Formats the SentimentResult for display
private fun formatResult(result: SentimentResult): String {
    val builder = StringBuilder()
    builder.appendLine("Sentiment: ${result.label}")
    builder.appendLine("Confidence: ${"%.2f".format(result.confidence * 100)}%")
    builder.appendLine()
    builder.appendLine("Probabilities:")
    result.probabilities
        .entries
        .sortedByDescending { it.value }
        .forEach { (label, prob) ->
            builder.appendLine("  $label: ${"%.2f".format(prob * 100)}%")
        }
    return builder.toString()
}
