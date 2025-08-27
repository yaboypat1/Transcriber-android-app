package com.example.session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.io.Closeable
import java.util.*

/**
 * Android SpeechRecognizer-based ASR implementation.
 * Provides real-time speech recognition with partial and final results.
 */
class AsrSession(private val context: Context) : Closeable {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var partialResult: String? = null
    private var finalResults = mutableListOf<String>()
    private var onPartialResult: ((String) -> Unit)? = null
    private var onFinalResult: ((String) -> Unit)? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            initializeSpeechRecognizer()
        }
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }

                override fun onBeginningOfSpeech() {
                    // Speech started
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level changed
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Audio buffer received
                }

                override fun onEndOfSpeech() {
                    isListening = false
                }

                override fun onError(error: Int) {
                    isListening = false
                    // Restart listening on certain errors
                    if (error == SpeechRecognizer.ERROR_NO_MATCH || 
                        error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                        startListening()
                    }
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val finalText = matches[0]
                        finalResults.add(finalText)
                        onFinalResult?.invoke(finalText)
                    }
                    // Restart listening for continuous recognition
                    startListening()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        partialResult = matches[0]
                        onPartialResult?.invoke(partialResult!!)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Handle speech events
                }
            })
        }
    }

    /** Start listening for speech input. */
    fun startListening() {
        if (speechRecognizer == null || isListening) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
        }
        
        speechRecognizer?.startListening(intent)
    }

    /** Stop listening for speech input. */
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    /** Set callback for partial results. */
    fun setOnPartialResult(callback: (String) -> Unit) {
        onPartialResult = callback
    }

    /** Set callback for final results. */
    fun setOnFinalResult(callback: (String) -> Unit) {
        onFinalResult = callback
    }

    /** Returns the latest partial transcription, if any. */
    fun getPartial(): String? = partialResult

    /** Returns a finalized transcription segment, if available. */
    fun consumeFinal(): String? {
        return if (finalResults.isNotEmpty()) {
            finalResults.removeAt(0)
        } else null
    }

    /** Check if currently listening. */
    fun isListening(): Boolean = isListening

    /** Releases the speech recognizer resources. */
    override fun close() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

