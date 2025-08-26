package com.example.listeners

import com.example.captions.CaptionManager
import com.example.session.AsrSession
import com.example.ingest.JitterBuffer
import com.example.ingest.JitterBuffer.AudioPacket

/**
 * Listens for messages coming from the wearable device and forwards
 * transcription updates to the [CaptionManager].
 */
class WearMessageListener(
    private val session: AsrSession,
    private val captions: CaptionManager,
    private val buffer: JitterBuffer
) {
    /** Feeds raw audio packets into the jitter buffer. */
    fun onAudioPacket(packet: AudioPacket) {
        buffer.add(packet)
    }

    /**
     * Call periodically to process buffered audio. Any resulting
     * transcription updates will be forwarded to the caption manager.
     */
    fun handleMessage() {
        buffer.nextFrame()?.let { session.receiveAudio(it) }
        session.getPartial()?.let { captions.onPartial(it) }
        session.getFinal()?.let { captions.onFinal(it) }
    }
}
