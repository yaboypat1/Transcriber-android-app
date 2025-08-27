package com.example.captions

/**
 * Manages live captions for real-time transcription display.
 * This interface allows for different caption implementations
 * (e.g., overlay, notification, accessibility).
 */
interface CaptionManager {
    /**
     * Called when a partial transcription is available.
     * This can be used to show live, updating captions.
     */
    fun onPartial(text: String)

    /**
     * Called when a final transcription segment is available.
     * This can be used to show final captions and trigger
     * storage or translation.
     */
    fun onFinal(text: String)
}

/**
 * Simple caption manager that logs transcriptions.
 * Replace this with your preferred caption implementation.
 */
class LoggingCaptionManager : CaptionManager {
    override fun onPartial(text: String) {
        println("Partial: $text")
    }

    override fun onFinal(text: String) {
        println("Final: $text")
    }
}
