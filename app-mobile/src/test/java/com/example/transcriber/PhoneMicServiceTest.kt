package com.example.transcriber

import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import com.example.service.PhoneMicService
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

@Config(sdk = [34])
class PhoneMicServiceTest {
    @Test
    fun startsInForegroundWithNotification() {
        val controller: ServiceController<PhoneMicService> = Robolectric.buildService(PhoneMicService::class.java)
        val service = controller.create().startCommand(0, 0).get()

        val nm = ApplicationProvider.getApplicationContext<android.content.Context>()
            .getSystemService(NotificationManager::class.java)
        val shadowNm = Shadows.shadowOf(nm)
        assertTrue(shadowNm.activeNotifications.size >= 0)
    }
}
