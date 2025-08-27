package com.example.transcriber

/**
 * Simple jitter buffer abstraction that yields PCM frames.
 */
class JitterBuffer {
    /**
     * Return the next PCM frame or null when none are available.
     */
    fun nextFrame(): ShortArray? {
        // Stub implementation
        return null
    }

    /**
     * Detect whether voice activity has ended for the current segment.
     */
    fun voiceActivityEnded(): Boolean {
        // Stub implementation
        return false
    }
}
