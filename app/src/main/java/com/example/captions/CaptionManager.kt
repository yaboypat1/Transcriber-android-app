package com.example.captions

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Coordinates caption updates between UI and persistent storage.
 *
 * @param uiUpdater invoked with every partial result to update the live captions
 * @param storage invoked with each final result to be persisted by a background job
 */
class CaptionManager(
    private val uiUpdater: (String) -> Unit,
    private val storage: (String) -> Unit
) {
    private val pendingFinal = ConcurrentLinkedQueue<String>()

    /**
     * Called when a partial transcription is available. The partial text
     * is forwarded directly to the UI updater for live captioning.
     */
    fun onPartial(text: String) {
        uiUpdater(text)
    }

    /**
     * Called when a final transcription is produced. The text is queued
     * for persistence and forwarded to the provided storage callback.
     */
    fun onFinal(text: String) {
        pendingFinal.add(text)
        storage(text)
    }

    /**
     * Exposes any queued final captions. Useful for testing or when the
     * storage job wants to drain items manually.
     */
    fun drainFinal(): List<String> {
        val result = mutableListOf<String>()
        while (true) {
            val next = pendingFinal.poll() ?: break
            result.add(next)
        }
        return result
    }
}
