package com.example.session

/**
 * Minimal representation of an ASR session. In a real implementation
 * this would interface with the speech recognition service.
 */
class AsrSession {
    companion object {
        init {
            System.loadLibrary("asr")
        }
    }
    private var partial: String? = null
    private var final: String? = null

    fun updatePartial(text: String) {
        partial = text
    }

    fun updateFinal(text: String) {
        final = text
    }

    /** Accepts raw PCM audio data for processing. */
    fun pushPcm(data: ByteArray) {
        // Audio ingestion would occur here in a real implementation.
    }

    /** Returns the latest partial transcription, if any. */
    fun getPartial(): String? = partial

    /** Returns the final transcription, if available. */
    fun getFinal(): String? = final

    /**
     * Returns the final transcription and clears it so the caller only
     * receives each final result once.
     */
    fun consumeFinal(): String? {
        val result = final
        final = null
        return result
    }
}
