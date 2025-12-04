package com.example.penjualan_produk_umkm

// File: TextPreprocessor.kt

// ============================================
// Helper script to create stemmer dictionary
// ============================================

/**
 * Run this Python script to create stemmer_dict.json
 * from your existing stemming results
 */

/*
import pandas as pd
from Sastrawi.Stemmer.StemmerFactory import StemmerFactory
import json
from tqdm import tqdm

# Load your data
df = pd.read_csv("cleaned_marketPlacesData.csv")

# Initialize stemmer
factory = StemmerFactory()
stemmer = factory.create_stemmer()

# Collect unique words
unique_words = set()
for tokens in tqdm(df['remove_stopwords_content']):
    if isinstance(tokens, str):
        unique_words.update(eval(tokens))

# Create stemmer dictionary
stemmer_dict = {}
for word in tqdm(unique_words):
    stemmer_dict[word] = stemmer.stem(word)

# Save to JSON
with open('stemmer_dict.json', 'w', encoding='utf-8') as f:
    json.dump(stemmer_dict, f, ensure_ascii=False, indent=2)

print(f"Stemmer dictionary created with {len(stemmer_dict)} words")
*/