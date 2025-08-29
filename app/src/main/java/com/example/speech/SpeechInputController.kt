package com.example.speech

import android.content.Context

interface SpeechInputController {
    fun start()
    fun stop()
    fun setOnPartial(callback: (String) -> Unit)
    fun setOnFinal(callback: (String) -> Unit)
    fun setOnError(callback: (String) -> Unit)
}

/**
 * Thin controller around a backend recognizer to simplify testing.
 */
class DefaultSpeechInputController(
    private val backend: RecognizerBackend
) : SpeechInputController {
    private var onPartial: (String) -> Unit = {}
    private var onFinal: (String) -> Unit = {}
    private var onError: (String) -> Unit = {}

    init {
        backend.onPartial { onPartial(it) }
        backend.onFinal { onFinal(it) }
        backend.onError {
            // Attempt a simple restart on backend error
            onError(it)
            backend.stop()
            backend.start()
        }
    }

    override fun start() {
        backend.start()
    }

    override fun stop() {
        backend.stop()
    }

    override fun setOnPartial(callback: (String) -> Unit) {
        onPartial = callback
    }

    override fun setOnFinal(callback: (String) -> Unit) {
        onFinal = callback
    }

    override fun setOnError(callback: (String) -> Unit) {
        onError = callback
    }
}

/** Backend abstraction to decouple Android SpeechRecognizer for tests. */
interface RecognizerBackend {
    fun start()
    fun stop()
    fun onPartial(callback: (String) -> Unit)
    fun onFinal(callback: (String) -> Unit)
    fun onError(callback: (String) -> Unit)
}

/** Simple fake backend for tests. */
class FakeRecognizerBackend : RecognizerBackend {
    private var onPartial: (String) -> Unit = {}
    private var onFinal: (String) -> Unit = {}
    private var onError: (String) -> Unit = {}

    var startedCount = 0
        private set
    var stoppedCount = 0
        private set

    override fun start() { startedCount++ }
    override fun stop() { stoppedCount++ }
    override fun onPartial(callback: (String) -> Unit) { onPartial = callback }
    override fun onFinal(callback: (String) -> Unit) { onFinal = callback }
    override fun onError(callback: (String) -> Unit) { onError = callback }

    // Helpers to drive events in tests
    fun emitPartial(text: String) = onPartial(text)
    fun emitFinal(text: String) = onFinal(text)
    fun emitError(msg: String) = onError(msg)
}

