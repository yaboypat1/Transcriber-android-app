package com.example.listeners

import com.example.captions.CaptionManager
import com.example.session.AsrSession

/**
 * Listens for messages coming from the wearable device and forwards
 * transcription updates to the [CaptionManager].
 */
class WearMessageListener(
    private val session: AsrSession,
    private val captions: CaptionManager
) {
    /**
     * Call whenever new data has arrived for this session. Any available
     * partial or final results will be forwarded to the caption manager.
     */
    fun handleMessage() {
        session.getPartial()?.let { captions.onPartial(it) }
        session.getFinal()?.let { captions.onFinal(it) }
    }
}
