package com.example.transcriber

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.postprocess.PostProcessWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PostProcessWorkerTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val config = androidx.work.Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun returnsFailureWhenNoSessionId() {
        val worker = TestListenableWorkerBuilder<PostProcessWorker>(context).build()
        val result = worker.startWork().get()
        assertTrue(result is ListenableWorker.Result.Failure)
    }

    @Test
    fun enqueueUniqueWorkUsesAppendOrReplace() {
        val wm = WorkManager.getInstance(context)
        val req1 = OneTimeWorkRequestBuilder<PostProcessWorker>().build()
        wm.enqueueUniqueWork("post_process_test", androidx.work.ExistingWorkPolicy.APPEND_OR_REPLACE, req1)
        val req2 = OneTimeWorkRequestBuilder<PostProcessWorker>().build()
        wm.enqueueUniqueWork("post_process_test", androidx.work.ExistingWorkPolicy.APPEND_OR_REPLACE, req2)

        val works = wm.getWorkInfosForUniqueWork("post_process_test").get()
        // Ensure there is at least one enqueued or running work
        assertTrue(works.isNotEmpty())
        assertTrue(works.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING })
    }
}
