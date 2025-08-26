package com.example.session

/**
 * Minimal representation of an ASR session. In a real implementation
 * this would interface with the speech recognition service.
 */
class AsrSession {
    private var partial: String? = null
    private var final: String? = null

    fun updatePartial(text: String) {
        partial = text
    }

    fun updateFinal(text: String) {
        final = text
    }

    /** Returns the latest partial transcription, if any. */
    fun getPartial(): String? = partial

    /** Returns the final transcription, if available. */
    fun getFinal(): String? = final
}
