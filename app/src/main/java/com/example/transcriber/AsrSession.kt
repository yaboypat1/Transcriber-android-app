package com.example.transcriber

/**
 * Stub for ASR session backed by native implementation.
 */
class AsrSession {
    init {
        // Ensure native library is loaded when session is created
        System.loadLibrary("asr")
    }

    /**
     * Feed a single PCM frame into the recognizer.
     */
    fun pushPcm(frame: ShortArray) {
        // Native method stub
    }

    /**
     * Retrieve partial transcription results.
     */
    fun getPartial(): String? {
        // Native method stub
        return null
    }

    /**
     * Retrieve finalised transcription segments.
     */
    fun getFinal(): String? {
        // Native method stub
        return null
    }
}
