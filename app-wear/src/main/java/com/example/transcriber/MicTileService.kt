package com.example.transcriber

import android.content.Intent
import android.util.Log
import androidx.wear.tiles.*
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.Chip
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.layout.Column
import androidx.wear.tiles.material.layout.PrimaryLayout
import androidx.wear.tiles.material.layout.Row
import androidx.wear.tiles.material.layout.SingleSlotLayout
import com.google.android.horologist.tiles.ExperimentalHorologistTilesApi
import com.google.android.horologist.tiles.SuspendingTileService

@OptIn(ExperimentalHorologistTilesApi::class)
class MicTileService : SuspendingTileService() {
    companion object {
        private const val TAG = "MicTileService"
        private const val ACTION_START_RECORDING = "START_RECORDING"
        private const val ACTION_STOP_RECORDING = "STOP_RECORDING"
    }

    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        Log.d(TAG, "Tile request received")
        
        return Tile.Builder()
            .setResourcesVersion("1")
            .setTileId("mic_tile")
            .setFreshnessIntervalMillis(1000L)
            .setLayout(
                PrimaryLayout.Builder()
                    .setPrimary(
                        Column.Builder()
                            .addContent(
                                Text.Builder()
                                    .setText("Transcriber")
                                    .setTypography(Typography.TYPOGRAPHY_TITLE_3)
                                    .build()
                            )
                            .addContent(
                                Row.Builder()
                                    .addContent(
                                        Button.Builder()
                                            .setId("start_btn")
                                            .setText("🎤")
                                            .setSize(Button.ButtonSize.SMALL)
                                            .build()
                                    )
                                    .addContent(
                                        Button.Builder()
                                            .setId("stop_btn")
                                            .setText("⏹️")
                                            .setSize(Button.ButtonSize.SMALL)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    override suspend fun onTileEnterEvent(requestParams: TileEnterEvent) {
        Log.d(TAG, "Tile entered")
    }

    override suspend fun onTileLeaveEvent(requestParams: TileLeaveEvent) {
        Log.d(TAG, "Tile left")
    }

    override suspend fun onTileRequest(requestParams: TileRequest): Tile {
        return tileRequest(requestParams)
    }

    override suspend fun onTileResourcesRequest(requestParams: TileResourcesRequest): TileResources {
        return TileResources.Builder()
            .setVersion("1")
            .build()
    }

    override suspend fun onTileClickEvent(requestParams: TileClickEvent) {
        val clickedElementId = requestParams.clickable.id
        
        when (clickedElementId) {
            "start_btn" -> {
                Log.d(TAG, "Start recording button clicked")
                startRecording()
            }
            "stop_btn" -> {
                Log.d(TAG, "Stop recording button clicked")
                stopRecording()
            }
        }
    }

    private fun startRecording() {
        try {
            // Send message to phone to start recording
            val intent = Intent(this, WearMicService::class.java).apply {
                action = ACTION_START_RECORDING
            }
            startService(intent)
            
            Log.d(TAG, "Recording started via tile")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording via tile", e)
        }
    }

    private fun stopRecording() {
        try {
            // Send message to phone to stop recording
            val intent = Intent(this, WearMicService::class.java).apply {
                action = ACTION_STOP_RECORDING
            }
            startService(intent)
            
            Log.d(TAG, "Recording stopped via tile")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording via tile", e)
        }
    }
}
