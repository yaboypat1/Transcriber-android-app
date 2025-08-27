package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ui.MicTileService
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.PutDataRequest
import java.nio.ByteBuffer
import com.example.service.OpusPacketQueue

/**
 * Service responsible for capturing audio from the watch microphone. The
 * service runs in the foreground with a persistent notification that exposes
 * mute and stop actions. The notification and foreground service type are
 * configured to satisfy Android 14+ microphone requirements.
 */
class WearMicService : Service(), MessageClient.OnMessageReceivedListener {
    companion object {
        const val ACTION_START = "com.example.service.action.START"
        const val ACTION_STOP = "com.example.service.action.STOP"
        const val ACTION_MUTE = "com.example.service.action.MUTE"

        private const val CHANNEL_ID = "wear_mic"
        private const val NOTIFICATION_ID = 1
        private const val PATH_AUDIO = "/audio"
        private const val PATH_ACK = "/ack"

        @Volatile
        var isRunning: Boolean = false
            private set
    }

    private var muted = false

    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }
    private var nodeId: String? = null
    private var packetQueue: OpusPacketQueue? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Initialize packetQueue after onCreate to avoid circular reference
        packetQueue = OpusPacketQueue { seq, data ->
            val id = nodeId
            if (id != null) {
                val payload = ByteBuffer.allocate(4 + data.size).putInt(seq).put(data).array()
                messageClient.sendMessage(id, PATH_AUDIO, payload)
            } else {
                packetQueue?.setConnected(false)
            }
        }
        
        messageClient.addListener(this)
        nodeClient.connectedNodes
            .addOnSuccessListener { nodes ->
                nodeId = nodes.firstOrNull()?.id
                packetQueue?.setConnected(nodeId != null)
            }
            .addOnFailureListener {
                packetQueue?.setConnected(false)
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_MUTE -> {
                muted = !muted
                updateNotification()
                return START_STICKY
            }
            else -> Unit
        }

        startForegroundService()
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = buildNotification()
        isRunning = true
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        MicTileService.requestUpdate(this)
    }

    /** Queue an Opus packet for transmission to the phone. */
    fun sendOpusPacket(data: ByteArray) {
        packetQueue?.queue(data)
        // For large bursts fall back to the DataClient which persists data.
        if ((packetQueue?.size() ?: 0) > 50) {
            val combined = (packetQueue?.pendingPackets() ?: emptyList()).fold(ByteArray(0)) { acc, arr ->
                acc + arr
            }
            val request = PutDataRequest.create(PATH_AUDIO).setData(combined).setUrgent()
            Wearable.getDataClient(this).putDataItem(request)
        }
    }

    private fun updateNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val muteIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, WearMicService::class.java).setAction(ACTION_MUTE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, WearMicService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (muted) "Muted" else "Recording"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Transcriber")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .addAction(0, if (muted) "Unmute" else "Mute", muteIntent)
            .addAction(0, "Stop", stopIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Microphone",
            NotificationManager.IMPORTANCE_LOW
        )
        nm.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        messageClient.removeListener(this)
        packetQueue?.setConnected(false)
        isRunning = false
        MicTileService.requestUpdate(this)
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == PATH_ACK) {
            val seq = ByteBuffer.wrap(event.data).int
            packetQueue?.ack(seq)
        }
    }

    override fun onBind(intent: Intent?) = null
}
