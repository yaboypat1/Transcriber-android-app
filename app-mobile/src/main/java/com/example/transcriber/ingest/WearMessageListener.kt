package com.example.transcriber.ingest

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.example.audio.AudioProcessor
import com.example.captions.EnhancedCaptionManager
import com.example.session.AsrSession
import com.example.language.LanguageIdentifier
import com.example.storage.TranscriptRepository
import com.example.storage.TranscriptSegment
import com.example.transcriber.translation.MlKitTranslatorPool
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

/**
 * Enhanced service that listens for audio messages from the watch and
 * processes them through the complete ASR pipeline.
 */
class WearMessageListener : LifecycleService(), MessageClient.OnMessageReceivedListener {
    companion object {
        private const val TAG = "WearMessageListener"
        private const val PATH_AUDIO = "/audio"
        private const val PATH_ACK = "/ack"
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var asrSession: AsrSession? = null
    private var languageIdentifier: LanguageIdentifier? = null
    private var translator: MlKitTranslatorPool? = null
    private var repository: TranscriptRepository? = null
    private var captionManager: EnhancedCaptionManager? = null

    private var isProcessing = false
    private var targetLanguage = "en" // Default target language

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WearMessageListener service created")
        
        // Initialize components
        initializeComponents()
        
        // Start listening for messages
        Wearable.getMessageClient(this).addListener(this)
    }

    private fun initializeComponents() {
        try {
            asrSession = AsrSession(this)
            languageIdentifier = LanguageIdentifier(this)
            translator = MlKitTranslatorPool
            repository = TranscriptRepository(this)
            captionManager = EnhancedCaptionManager(this, scope)

            // Set up ASR callbacks
            asrSession?.setOnPartialResult { text ->
                captionManager?.onPartial(text)
            }
            asrSession?.setOnFinalResult { text ->
                scope.launch {
                    processFinalResult(text)
                }
            }

            // Set up caption manager callbacks
            captionManager?.setOnLanguage { languageCode ->
                Log.d(TAG, "Language detected: $languageCode")
            }

            Log.d(TAG, "Components initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize components", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        // Start ASR session
        asrSession?.startListening()
        
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroying")
        
        // Cleanup resources
        asrSession?.close()
        languageIdentifier?.release()
        captionManager?.setOnError { }
        
        Wearable.getMessageClient(this).removeListener(this)
        super.onDestroy()
    }

    override fun onMessageReceived(event: MessageEvent) {
        Log.d(TAG, "Message received: ${event.path}")
        
        when (event.path) {
            PATH_AUDIO -> {
                if (!isProcessing) {
                    processAudioMessage(event.data)
                }
            }
            PATH_ACK -> {
                // Handle acknowledgment if needed
                val seq = ByteBuffer.wrap(event.data).int
                Log.d(TAG, "Acknowledgment received for sequence: $seq")
            }
        }
    }

    private fun processAudioMessage(audioData: ByteArray) {
        if (isProcessing) return
        
        isProcessing = true
        scope.launch {
            try {
                // Process audio through ASR
                // Note: With Android SpeechRecognizer, audio is handled automatically
                // This is mainly for handling watch audio packets
                
                Log.d(TAG, "Processing audio message of size: ${audioData.size}")
                
                // Simulate processing delay
                kotlinx.coroutines.delay(100)
                
                isProcessing = false
            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio message", e)
                captionManager?.onError("Audio processing failed: ${e.message}")
                isProcessing = false
            }
        }
    }

    private suspend fun processFinalResult(text: String) {
        try {
            // Identify language
            val detectedLanguage = languageIdentifier?.identifyLanguage(text)
            detectedLanguage?.let { lang ->
                captionManager?.onLanguageDetected(lang)
                Log.d(TAG, "Language detected: $lang")
            }

            // Translate if needed
            val translatedText = if (detectedLanguage != null && detectedLanguage != targetLanguage) {
                try {
                    translator?.translate(detectedLanguage, targetLanguage, text)
                } catch (e: Exception) {
                    Log.e(TAG, "Translation failed", e)
                    null
                }
            } else null

            // Store transcript
            val segment = TranscriptSegment(
                text = text,
                language = detectedLanguage,
                translatedText = translatedText,
                confidence = 0.9f
            )
            repository?.insertSegment(segment)

            // Show final result
            captionManager?.onFinal(text)

            Log.d(TAG, "Final result processed: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing final result", e)
            captionManager?.onError("Result processing failed: ${e.message}")
        }
    }

    /**
     * Set the target language for translation.
     */
    fun setTargetLanguage(languageCode: String) {
        targetLanguage = languageCode
        Log.d(TAG, "Target language set to: $languageCode")
    }
}
