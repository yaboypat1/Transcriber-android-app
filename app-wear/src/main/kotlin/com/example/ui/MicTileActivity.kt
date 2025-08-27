package com.example.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.service.WearMicService

/**
 * Minimal activity launched from the Tile click.
 * It toggles [WearMicService] start/stop and immediately finishes.
 */
class MicTileActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val running = WearMicService.isRunning
        val action = if (running) WearMicService.ACTION_STOP else WearMicService.ACTION_START

        startService(Intent(this, WearMicService::class.java).setAction(action))

        // Close immediately; this UI should not be visible to the user.
        finish()
        overridePendingTransition(0, 0)
    }
}

