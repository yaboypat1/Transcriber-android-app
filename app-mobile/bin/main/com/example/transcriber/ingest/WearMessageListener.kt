package com.example.transcriber.ingest

import androidx.lifecycle.LifecycleService
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

/**
 * Service registered on the phone that listens for audio messages from the
 * watch.  Real decoding and ASR/translation pipelines are left as TODOs.
 */
class WearMessageListener : LifecycleService(), MessageClient.OnMessageReceivedListener {
    override fun onCreate() {
        super.onCreate()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onDestroy() {
        Wearable.getMessageClient(this).removeListener(this)
        super.onDestroy()
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == "/audio") {
            // TODO: decode Opus, run ASR and translation
        }
    }
}
