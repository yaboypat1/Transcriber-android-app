package com.example.captions

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages live captions for real-time transcription display.
 * This interface allows for different caption implementations
 * (e.g., overlay, notification, accessibility).
 */
interface CaptionManager {
    /**
     * Called when a partial transcription is available.
     * This can be used to show live, updating captions.
     */
    fun onPartial(text: String)

    /**
     * Called when a final transcription segment is available.
     * This can be used to show final captions and trigger
     * storage or translation.
     */
    fun onFinal(text: String)

    /**
     * Called when language is detected.
     */
    fun onLanguageDetected(languageCode: String)

    /**
     * Called when an error occurs.
     */
    fun onError(error: String)
}

/**
 * Enhanced caption manager with multiple display options.
 */
class EnhancedCaptionManager(
    private val context: Context,
    private val scope: CoroutineScope
) : CaptionManager {
    companion object {
        private const val TAG = "EnhancedCaptionManager"
    }

    private var onPartialCallback: ((String) -> Unit)? = null
    private var onFinalCallback: ((String) -> Unit)? = null
    private var onLanguageCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    override fun onPartial(text: String) {
        Log.d(TAG, "Partial: $text")
        onPartialCallback?.invoke(text)
        
        // Show partial result as toast for immediate feedback
        scope.launch(Dispatchers.Main) {
            Toast.makeText(context, "Partial: $text", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFinal(text: String) {
        Log.d(TAG, "Final: $text")
        onFinalCallback?.invoke(text)
        
        // Show final result as longer toast
        scope.launch(Dispatchers.Main) {
            Toast.makeText(context, "Final: $text", Toast.LENGTH_LONG).show()
        }
    }

    override fun onLanguageDetected(languageCode: String) {
        Log.d(TAG, "Language detected: $languageCode")
        onLanguageCallback?.invoke(languageCode)
        
        // Show language detection notification
        scope.launch(Dispatchers.Main) {
            Toast.makeText(context, "Language: $languageCode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(error: String) {
        Log.e(TAG, "Error: $error")
        onErrorCallback?.invoke(error)
        
        // Show error notification
        scope.launch(Dispatchers.Main) {
            Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Set callback for partial results.
     */
    fun setOnPartial(callback: (String) -> Unit) {
        onPartialCallback = callback
    }

    /**
     * Set callback for final results.
     */
    fun setOnFinal(callback: (String) -> Unit) {
        onFinalCallback = callback
    }

    /**
     * Set callback for language detection.
     */
    fun setOnLanguage(callback: (String) -> Unit) {
        onLanguageCallback = callback
    }

    /**
     * Set callback for errors.
     */
    fun setOnError(callback: (String) -> Unit) {
        onErrorCallback = callback
    }
}

/**
 * Simple caption manager that logs transcriptions.
 * Replace this with your preferred caption implementation.
 */
class LoggingCaptionManager : CaptionManager {
    override fun onPartial(text: String) {
        println("Partial: $text")
    }

    override fun onFinal(text: String) {
        println("Final: $text")
    }

    override fun onLanguageDetected(languageCode: String) {
        println("Language: $languageCode")
    }

    override fun onError(error: String) {
        println("Error: $error")
    }
}
