package com.example.postprocess

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf

/**
 * Background worker that revisits recorded sessions with a heavier ASR model
 * to improve accuracy of stored transcripts.
 */
class PostProcessWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString(KEY_SESSION_ID) ?: return Result.failure()
        return try {
            val audio = fetchSessionAudio(sessionId)
            val transcript = runHeavyAsr(audio)
            updateTranscript(sessionId, transcript)
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    private suspend fun fetchSessionAudio(sessionId: String): ByteArray {
        // TODO: Retrieve stored PCM audio for the given session ID.
        return ByteArray(0)
    }

    private suspend fun runHeavyAsr(audio: ByteArray): String {
        // TODO: Execute a higher accuracy ASR model over the audio.
        return ""
    }

    private fun updateTranscript(sessionId: String, text: String) {
        // TODO: Persist the refined transcript text back to storage.
    }

    companion object {
        private const val KEY_SESSION_ID = "session_id"

        /**
         * Schedules this worker with charging and unmetered network constraints.
         */
        fun schedule(context: Context, sessionId: String) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val request = OneTimeWorkRequestBuilder<PostProcessWorker>()
                .setInputData(workDataOf(KEY_SESSION_ID to sessionId))
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "post_process_" + sessionId,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}

