package com.example.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.RequestBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.TileService
import androidx.wear.protolayout.TileBuilders
import com.example.service.WearMicService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * Simple tile that toggles [WearMicService] on the watch.
 * Uses the new protolayout API as recommended by Google.
 */
class MicTileService : TileService() {

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        val running = WearMicService.isRunning
        val label = if (running) "Stop" else "Start"
        val action = if (running) WearMicService.ACTION_STOP else WearMicService.ACTION_START

        val pi = PendingIntent.getService(
            this,
            0,
            Intent(this, WearMicService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val clickable = ModifiersBuilders.Clickable.Builder()
            .setOnClick(ActionBuilders.AndroidPendingIntent.Builder(pi).build())
            .build()

        val text = LayoutElementBuilders.Text.Builder()
            .setText(label)
            .build()

        val box = LayoutElementBuilders.Box.Builder()
            .setModifiers(ModifiersBuilders.Modifiers.Builder().setClickable(clickable).build())
            .addContent(text)
            .build()

        val layout = LayoutElementBuilders.Layout.Builder()
            .setRoot(box)
            .build()

        val timeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(layout)
                    .build()
            )
            .build()

        return Futures.immediateFuture(
            TileBuilders.Tile.Builder()
                .setResourcesVersion("1")
                .setTimeline(timeline)
                .build()
        )
    }

    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<TileBuilders.Resources> {
        return Futures.immediateFuture(
            TileBuilders.Resources.Builder()
                .setVersion("1")
                .build()
        )
    }

    companion object {
        fun requestUpdate(context: Context) {
            try {
                getUpdater(context).requestUpdate(MicTileService::class.java)
            } catch (e: Exception) {
                // Ignore errors for now
            }
        }
    }
}
