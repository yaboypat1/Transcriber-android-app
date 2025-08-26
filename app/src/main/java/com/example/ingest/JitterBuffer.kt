package com.example.ingest

import java.util.PriorityQueue

/** Simple jitter buffer for audio packets. */
class JitterBuffer(private val playoutDelayMs: Long = 50) {
    data class AudioPacket(val timestampMs: Long, val data: ByteArray)

    private val queue = PriorityQueue<AudioPacket>(compareBy { it.timestampMs })

    /** Adds a packet to the buffer. */
    fun add(packet: AudioPacket) {
        queue.add(packet)
    }

    /**
     * Returns the next frame if it is ready for playout based on the configured
     * delay; otherwise returns null.
     */
    fun nextFrame(nowMs: Long = System.currentTimeMillis()): ByteArray? {
        val packet = queue.peek() ?: return null
        return if (packet.timestampMs + playoutDelayMs <= nowMs) {
            queue.poll().data
        } else {
            null
        }
    }
}
