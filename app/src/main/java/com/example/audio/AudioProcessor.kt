package com.example.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Advanced audio processor that handles real-time audio recording,
 * voice activity detection, and audio buffering.
 */
class AudioProcessor(
    private val context: Context,
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    companion object {
        private const val TAG = "AudioProcessor"
        private const val BUFFER_SIZE_MULTIPLIER = 4
        private const val VAD_THRESHOLD = 0.01f
        private const val SILENCE_DURATION_MS = 1000L
        private const val FRAME_SIZE_MS = 20L
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var onAudioFrame: ((ShortArray) -> Unit)? = null
    private var onVoiceActivity: ((Boolean) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    private val frameSize = (sampleRate * FRAME_SIZE_MS / 1000).toInt()
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * BUFFER_SIZE_MULTIPLIER
    
    private var lastVoiceActivity = false
    private var silenceStartTime = 0L

    /**
     * Start audio recording and processing.
     */
    fun startRecording() {
        if (isRecording) return

        try {
            audioRecord = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(audioFormat)
                        .setChannelMask(channelConfig)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("AudioRecord not initialized")
            }

            isRecording = true
            recordingJob = scope.launch {
                processAudio()
            }
            
            Log.d(TAG, "Audio recording started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            onError?.invoke("Failed to start recording: ${e.message}")
        }
    }

    /**
     * Stop audio recording.
     */
    fun stopRecording() {
        if (!isRecording) return

        isRecording = false
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        
        Log.d(TAG, "Audio recording stopped")
    }

    /**
     * Set callback for audio frames.
     */
    fun setOnAudioFrame(callback: (ShortArray) -> Unit) {
        onAudioFrame = callback
    }

    /**
     * Set callback for voice activity changes.
     */
    fun setOnVoiceActivity(callback: (Boolean) -> Unit) {
        onVoiceActivity = callback
    }

    /**
     * Set callback for errors.
     */
    fun setOnError(callback: (String) -> Unit) {
        onError = callback
    }

    /**
     * Check if currently recording.
     */
    fun isRecording(): Boolean = isRecording

    /**
     * Get current audio level (RMS).
     */
    fun getAudioLevel(): Float {
        return audioRecord?.let { record ->
            val buffer = ShortArray(frameSize)
            val read = record.read(buffer, 0, buffer.size)
            if (read > 0) {
                calculateRMS(buffer, read)
            } else 0f
        } ?: 0f
    }

    private suspend fun processAudio() {
        val buffer = ShortArray(frameSize)
        val record = audioRecord ?: return

        record.startRecording()
        
        while (isRecording && record.state == AudioRecord.STATE_INITIALIZED) {
            try {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) {
                    val frame = buffer.copyOf(read)
                    processAudioFrame(frame)
                }
                delay(FRAME_SIZE_MS)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio frame", e)
                onError?.invoke("Audio processing error: ${e.message}")
                break
            }
        }
    }

    private fun processAudioFrame(frame: ShortArray) {
        // Calculate audio level for VAD
        val level = calculateRMS(frame, frame.size)
        val hasVoice = level > VAD_THRESHOLD
        
        // Handle voice activity detection
        if (hasVoice != lastVoiceActivity) {
            lastVoiceActivity = hasVoice
            onVoiceActivity?.invoke(hasVoice)
            
            if (!hasVoice) {
                silenceStartTime = System.currentTimeMillis()
            }
        }
        
        // Send audio frame to callback
        onAudioFrame?.invoke(frame)
    }

    private fun calculateRMS(buffer: ShortArray, length: Int): Float {
        var sum = 0.0
        for (i in 0 until length) {
            sum += buffer[i] * buffer[i]
        }
        return sqrt(sum / length).toFloat() / Short.MAX_VALUE
    }

    /**
     * Check if voice activity has ended (silence detected).
     */
    fun hasVoiceActivityEnded(): Boolean {
        return !lastVoiceActivity && 
               (System.currentTimeMillis() - silenceStartTime) > SILENCE_DURATION_MS
    }

    /**
     * Release resources.
     */
    fun release() {
        stopRecording()
        scope.cancel()
    }
}
