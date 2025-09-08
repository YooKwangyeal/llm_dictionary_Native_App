package com.example.openary

data class DictionaryWord(
    val word: String,
    val pos: String?,
    val length: Int?,
    val rarity: Int?,
    val score: Double?,
    val confidence: Double?,
    val definition: String?,
    val example: String?
)
