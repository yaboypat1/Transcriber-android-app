package com.example.listeners

import com.example.captions.CaptionManager
import com.example.ingest.JitterBuffer
import com.example.ingest.JitterBuffer.AudioPacket
import com.example.session.AsrSession
import com.example.storage.TranscriptRepository
import com.example.storage.TranscriptSegment

/**
 * Listens for audio coming from the wearable device. Incoming packets are
 * buffered briefly to compensate for network jitter before being forwarded to
 * the [AsrSession]. Interim and final transcripts are delivered through the
 * provided [CaptionManager].
 */
class WearMessageListener(
    private val captions: CaptionManager,
    private val repository: TranscriptRepository,
    jitterMs: Long = 50L
) {
    private val buffer = JitterBuffer(jitterMs)
    private var session: AsrSession? = null

    /** Starts a new recording session. */
    fun startRecording() {
        session = AsrSession()
    }

    /** Stops the current recording session. */
    fun stopRecording() {
        session?.consumeFinal()?.let {
            captions.onFinal(it)
            repository.insertSegment(TranscriptSegment(text = it))
        }
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
            s.consumeFinal()?.let {
                captions.onFinal(it)
                repository.insertSegment(TranscriptSegment(text = it))
            }
            frame = buffer.nextFrame()
        }
        s.consumeFinal()?.let {
            captions.onFinal(it)
            repository.insertSegment(TranscriptSegment(text = it))
        }
    }
}
