package com.example.session

import java.io.Closeable

/**
 * Thin wrapper around the native ASR engine. Raw PCM audio is pushed into the
 * engine and partial/final transcripts are retrieved via JNI bindings.
 */
class AsrSession : Closeable {
    companion object {
        init {
            System.loadLibrary("asr")
        }
    }

    private var handle: Long = nativeCreate()

    /** Feeds a chunk of 16-bit PCM audio to the recognizer. */
    fun pushPcm(data: ByteArray) {
        nativePush(handle, data, data.size)
    }

    /** Returns the latest partial transcription, if any. */
    fun getPartial(): String? = nativePartial(handle)

    /** Returns a finalized transcription segment, if available. */
    fun consumeFinal(): String? = nativeFinal(handle)

    /** Releases the native resources backing this session. */
    override fun close() {
        if (handle != 0L) {
            nativeClose(handle)
            handle = 0L
        }
    }

    private external fun nativeCreate(): Long
    private external fun nativePush(handle: Long, data: ByteArray, length: Int)
    private external fun nativePartial(handle: Long): String?
    private external fun nativeFinal(handle: Long): String?
    private external fun nativeClose(handle: Long)
}

