package com.example.language

import android.content.Context
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.tasks.await

/**
 * Service for identifying the language of spoken text.
 * Uses ML Kit Language Identification for accurate language detection.
 */
class LanguageIdentifier(private val context: Context) {
    companion object {
        private const val TAG = "LanguageIdentifier"
        private const val CONFIDENCE_THRESHOLD = 0.5f
    }

    private val languageIdentifier: LanguageIdentifier by lazy {
        LanguageIdentification.getClient(LanguageIdentifierOptions.Builder()
            .setConfidenceThreshold(CONFIDENCE_THRESHOLD)
            .build())
    }

    /**
     * Identify the language of the given text.
     * @param text The text to identify the language for
     * @return The detected language code (e.g., "en", "es", "fr") or null if detection fails
     */
    suspend fun identifyLanguage(text: String): String? {
        return try {
            if (text.isBlank()) return null
            
            val result = languageIdentifier.identifyLanguage(text).await()
            Log.d(TAG, "Language detected: $result for text: '${text.take(50)}...'")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Language identification failed", e)
            null
        }
    }

    /**
     * Identify possible languages with confidence scores.
     * @param text The text to identify languages for
     * @return List of possible languages with confidence scores
     */
    suspend fun identifyPossibleLanguages(text: String): List<LanguageConfidence> {
        return try {
            if (text.isBlank()) return emptyList()
            
            val results = languageIdentifier.identifyAllLanguages(text).await()
            results.map { LanguageConfidence(it.languageTag, it.confidence) }
                .sortedByDescending { it.confidence }
        } catch (e: Exception) {
            Log.e(TAG, "Multiple language identification failed", e)
            emptyList()
        }
    }

    /**
     * Check if a specific language is supported.
     * @param languageCode The language code to check (e.g., "en", "es")
     * @return True if the language is supported
     */
    fun isLanguageSupported(languageCode: String): Boolean {
        return try {
            val conditions = DownloadConditions.Builder().build()
            languageIdentifier.downloadModelIfNeeded(languageCode, conditions).isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Language support check failed for: $languageCode", e)
            false
        }
    }

    /**
     * Get the display name for a language code.
     * @param languageCode The language code (e.g., "en", "es")
     * @return The display name of the language
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "es" -> "Spanish"
            "fr" -> "French"
            "de" -> "German"
            "it" -> "Italian"
            "pt" -> "Portuguese"
            "ru" -> "Russian"
            "ja" -> "Japanese"
            "ko" -> "Korean"
            "zh" -> "Chinese"
            "ar" -> "Arabic"
            "hi" -> "Hindi"
            "tr" -> "Turkish"
            "nl" -> "Dutch"
            "pl" -> "Polish"
            "sv" -> "Swedish"
            "da" -> "Danish"
            "no" -> "Norwegian"
            "fi" -> "Finnish"
            "cs" -> "Czech"
            "hu" -> "Hungarian"
            "ro" -> "Romanian"
            "bg" -> "Bulgarian"
            "hr" -> "Croatian"
            "sk" -> "Slovak"
            "sl" -> "Slovenian"
            "et" -> "Estonian"
            "lv" -> "Latvian"
            "lt" -> "Lithuanian"
            else -> languageCode.uppercase()
        }
    }

    /**
     * Release resources.
     */
    fun release() {
        languageIdentifier.close()
    }

    /**
     * Data class representing a language with confidence score.
     */
    data class LanguageConfidence(
        val languageCode: String,
        val confidence: Float
    )
}
