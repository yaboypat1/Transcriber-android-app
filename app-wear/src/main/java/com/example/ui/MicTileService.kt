package com.example.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.Tile
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TimelineBuilders
import androidx.wear.tiles.TileService
import com.example.service.WearMicService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * Simple tile that toggles [WearMicService] on the watch. The tile displays a
 * Start or Stop label depending on the running state of the service and sends
 * the corresponding action when tapped.
 */
class MicTileService : TileService() {

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<Tile> {
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
            .setFontStyle(LayoutElementBuilders.FontStyle.Builder().build())
            .build()

        val box = LayoutElementBuilders.Box.Builder()
            .setModifiers(ModifiersBuilders.Modifiers.Builder().setClickable(clickable).build())
            .addContent(text)
            .build()

        val layout = TileBuilders.Layout.Builder()
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
        /** Requests an update for the tile so its state reflects the service. */
        fun requestUpdate(context: Context) {
            getUpdater(context).requestUpdate(MicTileService::class.java)
        }
    }
}
