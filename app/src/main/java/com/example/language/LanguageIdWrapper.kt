package com.example.language

import android.content.Context
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.tasks.await

/**
 * Thin wrapper over ML Kit Language ID that caches the detector and handles
 * model readiness. All operations are on-device.
 */
class LanguageIdWrapper(
    context: Context,
    private val provider: () -> LanguageIdentifier = { LanguageIdentification.getClient() }
) {
    private val detector: LanguageIdentifier by lazy(provider)
    @Volatile private var isReady: Boolean = true // ML Kit LID is on-device; assume ready by default

    suspend fun identify(text: String): String? {
        if (text.isBlank()) return null
        if (!isReady) return null
        return try {
            val lang = detector.identifyLanguage(text).await()
            if (lang == "und") null else lang
        } catch (_: Exception) {
            // If model is not ready yet or failed, mark not ready temporarily
            isReady = false
            null
        }
    }

    fun markModelReady() { isReady = true }
    fun markModelNotReady() { isReady = false }
    fun close() { detector.close() }
}

