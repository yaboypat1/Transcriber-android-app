package com.example.transcriber

import com.example.session.AsrSession
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Handles audio recording and streaming frames into the ASR engine.
 */
class Recorder(private val jitterBuffer: JitterBuffer) {
    private var session: AsrSession? = null

    /**
     * Start capturing audio and create a new ASR session.
     */
    fun startRecording() {
        session = AsrSession()
    }

    /**
     * Process the next available frame from the jitter buffer.
     * This should be called repeatedly while recording.
     */
    fun onAudioFrame() {
        val frame = jitterBuffer.nextFrame() ?: return
        val asr = session ?: return

        // Convert 16-bit PCM samples to a byte array for the session API
        val byteBuffer = ByteBuffer.allocate(frame.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        frame.forEach { sample -> byteBuffer.putShort(sample) }
        asr.pushPcm(byteBuffer.array())

        // Retrieve partial result after each frame
        val partial = asr.getPartial()
        if (partial != null) {
            handlePartial(partial)
        }

        // On voice activity end, fetch finalised text
        if (jitterBuffer.voiceActivityEnded()) {
            val finalText = asr.consumeFinal()
            if (finalText != null) {
                handleFinal(finalText)
            }
        }
    }

    private fun handlePartial(text: String) {
        // Hook for UI update or logging
    }

    private fun handleFinal(text: String) {
        // Hook for UI update or logging
    }
}
