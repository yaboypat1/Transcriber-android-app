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
import com.example.session.AsrSession
import com.example.storage.TranscriptRepository
import com.example.storage.TranscriptSegment
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private suspend fun fetchSessionAudio(sessionId: String): ByteArray =
        withContext(Dispatchers.IO) {
            val file = File(applicationContext.filesDir, "$sessionId.pcm")
            if (file.exists()) file.readBytes() else ByteArray(0)
        }

    private suspend fun runHeavyAsr(audio: ByteArray): String =
        withContext(Dispatchers.Default) {
            AsrSession().use { session ->
                session.pushPcm(audio)
                val builder = StringBuilder()
                while (true) {
                    val segment = session.consumeFinal() ?: break
                    if (builder.isNotEmpty()) builder.append(' ')
                    builder.append(segment)
                }
                if (builder.isNotEmpty()) builder.toString() else session.getPartial().orEmpty()
            }
        }

    private suspend fun updateTranscript(sessionId: String, text: String) =
        withContext(Dispatchers.IO) {
            val repo = TranscriptRepository(applicationContext)
            repo.insertSegment(TranscriptSegment(text = text))
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

