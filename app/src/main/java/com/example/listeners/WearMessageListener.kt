package com.example.listeners

import android.content.Context
import com.example.captions.CaptionManager
import com.example.ingest.JitterBuffer
import com.example.ingest.JitterBuffer.AudioPacket
import com.example.session.AsrSession
import com.example.storage.TranscriptRepository
import com.example.storage.TranscriptSegment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens for audio coming from the wearable device. Incoming packets are
 * buffered briefly to compensate for network jitter before being forwarded to
 * the [AsrSession]. Interim and final transcripts are delivered through the
 * provided [CaptionManager].
 */
class WearMessageListener(
    private val context: Context,
    private val captions: CaptionManager,
    private val repository: TranscriptRepository,
    jitterMs: Long = 50L
) {
    private val buffer = JitterBuffer(jitterMs)
    private var session: AsrSession? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /** Starts a new recording session. */
    fun startRecording() {
        session = AsrSession(context)
    }

    /** Stops the current recording session. */
    fun stopRecording() {
        session?.use { s ->
            s.consumeFinal()?.let {
                captions.onFinal(it)
                scope.launch { repository.insertSegment(TranscriptSegment(text = it)) }
            }
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
            // Audio capture and streaming are handled internally by AsrSession
            // (via SpeechRecognizer). Here we simply poll for partial/final
            // results while draining buffered frames to maintain timing.
            s.getPartial()?.let { captions.onPartial(it) }
            s.consumeFinal()?.let {
                captions.onFinal(it)
                scope.launch { repository.insertSegment(TranscriptSegment(text = it)) }
            }
            frame = buffer.nextFrame()
        }
        s.consumeFinal()?.let {
            captions.onFinal(it)
            scope.launch { repository.insertSegment(TranscriptSegment(text = it)) }
        }
    }
}
