package com.example.listeners

import com.example.captions.CaptionManager
import com.example.ingest.JitterBuffer
import com.example.ingest.JitterBuffer.AudioPacket
import com.example.session.AsrSession

/**
 * Listens for messages coming from the wearable device and forwards
 * transcription updates to the [CaptionManager].
 */
class WearMessageListener(
    private val captions: CaptionManager,
    private val buffer: JitterBuffer
) {
    private var session: AsrSession? = null

    /** Starts a new recording session. */
    fun startRecording() {
        session = AsrSession()
    }

    /** Stops the current recording session. */
    fun stopRecording() {
        session?.consumeFinal()?.let { captions.onFinal(it) }
        session = null
    }

    /** Feeds raw audio packets into the jitter buffer. */
    fun onAudioPacket(packet: AudioPacket) {
        if (session == null) startRecording()
        buffer.add(packet)
    }

    /**
     * Call periodically to process buffered audio. Any resulting
     * transcription updates will be forwarded to the caption manager.
     */
    fun handleMessage() {
        val s = session ?: return
        var frame = buffer.nextFrame()
        while (frame != null) {
            s.pushPcm(frame)
            s.getPartial()?.let { captions.onPartial(it) }
            frame = buffer.nextFrame()
        }
        s.consumeFinal()?.let { captions.onFinal(it) }
    }
}
