package com.example.transcriber

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

        asr.pushPcm(frame)

        // Retrieve partial result after each frame
        val partial = asr.getPartial()
        if (partial != null) {
            handlePartial(partial)
        }

        // On voice activity end, fetch finalised text
        if (jitterBuffer.voiceActivityEnded()) {
            val finalText = asr.getFinal()
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
