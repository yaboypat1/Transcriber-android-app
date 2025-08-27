package com.example.transcriber.audio

import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.google.android.gms.wearable.Wearable

/**
 * Foreground service that captures microphone audio on the watch and streams it
 * to the paired phone using the Wear OS Data Layer.  The implementation here is
 * only a skeleton: real encoding, VAD and message sending are omitted for
 * brevity.
 */
class WearMicService : Service() {
    private lateinit var recorder: AudioRecord
    private var sendingJob: Job? = null
    private val msgClient by lazy { Wearable.getMessageClient(this) }
    private val notifId = 42

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationCompat.Builder(this, "mic")
            .setContentTitle("Mic active")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
        startForeground(notifId, notification)

        recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(16000)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
            )
            .setBufferSizeInBytes(
                4 * AudioRecord.getMinBufferSize(
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
            )
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (sendingJob == null) sendingJob = startStreaming()
        return START_STICKY
    }

    private fun startStreaming() = CoroutineScope(Dispatchers.Default).launch {
        val buffer = ShortArray(320) // 20ms at 16kHz
        recorder.startRecording()
        while (isActive) {
            val n = recorder.read(buffer, 0, buffer.size)
            if (n > 0) {
                // TODO: encode with Opus and send via Data Layer
            }
        }
    }

    override fun onDestroy() {
        sendingJob?.cancel()
        recorder.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
