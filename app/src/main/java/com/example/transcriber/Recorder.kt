package com.example.transcriber

import android.content.Context
import android.util.Log
import com.example.audio.AudioProcessor
import com.example.ingest.JitterBuffer
import com.example.session.AsrSession
import com.example.language.LanguageIdentifier
import com.example.storage.TranscriptRepository
import com.example.storage.TranscriptSegment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Advanced recorder that handles audio recording, ASR, language identification,
 * and translation in a unified pipeline.
 */
class Recorder(
    private val context: Context,
    private val jitterBuffer: JitterBuffer,
    private val repository: TranscriptRepository
) {
    companion object {
        private const val TAG = "Recorder"
    }

    private var session: AsrSession? = null
    private var audioProcessor: AudioProcessor? = null
    private var languageIdentifier: LanguageIdentifier? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private var onPartialResult: ((String) -> Unit)? = null
    private var onFinalResult: ((String, String?) -> Unit)? = null
    private var onLanguageDetected: ((String) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    /**
     * Start capturing audio and create a new ASR session.
     */
    fun startRecording() {
        try {
            // Initialize components
            session = AsrSession(context)
            audioProcessor = AudioProcessor(context)
            languageIdentifier = LanguageIdentifier(context)

            // Set up ASR callbacks
            session?.setOnPartialResult { text ->
                onPartialResult?.invoke(text)
            }
            session?.setOnFinalResult { text ->
                scope.launch {
                    processFinalResult(text)
                }
            }

            // Set up audio processor callbacks
            audioProcessor?.setOnAudioFrame { frame ->
                // Convert audio frame to jitter buffer format
                // Note: AudioPacket is an inner class of JitterBuffer
                // For now, we'll skip this since SpeechRecognizer handles audio automatically
            }
            audioProcessor?.setOnVoiceActivity { hasVoice ->
                // Handle voice activity changes
                if (!hasVoice && audioProcessor?.hasVoiceActivityEnded() == true) {
                    session?.stopListening()
                }
            }
            audioProcessor?.setOnError { error ->
                onError?.invoke("Audio error: $error")
            }

            // Start audio processing and ASR
            audioProcessor?.startRecording()
            session?.startListening()

            Log.d(TAG, "Recording started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            onError?.invoke("Failed to start recording: ${e.message}")
        }
    }

    /**
     * Stop recording and cleanup resources.
     */
    fun stopRecording() {
        try {
            session?.stopListening()
            audioProcessor?.stopRecording()
            
            // Get final result if available
            session?.consumeFinal()?.let { finalText ->
                scope.launch {
                    processFinalResult(finalText)
                }
            }

            Log.d(TAG, "Recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            onError?.invoke("Error stopping recording: ${e.message}")
        }
    }

    /**
     * Process the next available frame from the jitter buffer.
     * This method is called periodically to process buffered audio.
     */
    fun onAudioFrame() {
        val frame = jitterBuffer.nextFrame() ?: return
        
        // Process audio frame through ASR
        // Note: With Android SpeechRecognizer, we don't need to manually feed audio
        // The system handles the audio input automatically
    }

    private suspend fun processFinalResult(text: String) {
        try {
            // Identify language
            val detectedLanguage = languageIdentifier?.identifyLanguage(text)
            detectedLanguage?.let { lang ->
                onLanguageDetected?.invoke(lang)
                Log.d(TAG, "Language detected: $lang")
            }

            // Store transcript
            val segment = TranscriptSegment(
                text = text,
                language = detectedLanguage,
                confidence = 0.9f // Default confidence for now
            )
            repository.insertSegment(segment)

            // Notify final result
            onFinalResult?.invoke(text, detectedLanguage)

            Log.d(TAG, "Final result processed: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing final result", e)
            onError?.invoke("Error processing final result: ${e.message}")
        }
    }

    /**
     * Set callback for partial results.
     */
    fun setOnPartialResult(callback: (String) -> Unit) {
        onPartialResult = callback
    }

    /**
     * Set callback for final results.
     */
    fun setOnFinalResult(callback: (String, String?) -> Unit) {
        onFinalResult = callback
    }

    /**
     * Set callback for language detection.
     */
    fun setOnLanguageDetected(callback: (String) -> Unit) {
        onLanguageDetected = callback
    }

    /**
     * Set callback for errors.
     */
    fun setOnError(callback: (String) -> Unit) {
        onError = callback
    }

    /**
     * Release all resources.
     */
    fun release() {
        stopRecording()
        session?.close()
        audioProcessor?.release()
        languageIdentifier?.release()
    }
}
