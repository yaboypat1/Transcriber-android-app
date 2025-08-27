package com.example.ingest

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.DelayQueue
import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit

/**
 * Buffers incoming audio packets to compensate for network jitter.
 * Packets are held for a configurable delay before being made available
 * for processing.
 */
class JitterBuffer(private val delayMs: Long) {
    private val buffer = DelayQueue<AudioPacket>()
    private var lastVoiceActivity = false
    private var voiceActivityEnded = false

    /**
     * Adds an audio packet to the buffer. The packet will become
     * available after the configured delay.
     */
    fun add(packet: AudioPacket) {
        buffer.offer(packet)
    }

    /**
     * Returns the next available audio frame, or null if none are ready.
     */
    fun nextFrame(): ShortArray? {
        val packet = buffer.poll() ?: return null
        
        // Check for voice activity changes
        val currentVad = packet.voiceActivity
        if (lastVoiceActivity && !currentVad) {
            voiceActivityEnded = true
        }
        lastVoiceActivity = currentVad
        
        return packet.audioData
    }

    /**
     * Returns true if voice activity has ended since the last call.
     */
    fun voiceActivityEnded(): Boolean {
        val result = voiceActivityEnded
        voiceActivityEnded = false
        return result
    }

    /**
     * Represents an audio packet with voice activity detection.
     */
    data class AudioPacket(
        val audioData: ShortArray,
        val voiceActivity: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    ) : Delayed {
        override fun getDelay(unit: TimeUnit): Long {
            val elapsed = System.currentTimeMillis() - timestamp
            return unit.convert(delayMs - elapsed, TimeUnit.MILLISECONDS)
        }

        override fun compareTo(other: Delayed): Int {
            return (getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS)).toInt()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as AudioPacket
            return timestamp == other.timestamp
        }

        override fun hashCode(): Int = timestamp.hashCode()
    }
}
