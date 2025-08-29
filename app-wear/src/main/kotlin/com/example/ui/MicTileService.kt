package com.example.ui

import android.content.Context
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TimelineBuilders
import androidx.wear.tiles.TileService
import com.example.service.WearMicService
import androidx.concurrent.futures.CallbackToFutureAdapter
import com.google.common.util.concurrent.ListenableFuture

/**
 * Simple tile that toggles [WearMicService] on the watch. The tile displays a
 * Start or Stop label depending on the running state of the service and
 * launches a tiny activity that starts/stops the service when tapped.
 */
class MicTileService : TileService() {

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<TileBuilders.Tile> =
        CallbackToFutureAdapter.getFuture { completer ->
            val running = WearMicService.isRunning
            val label = if (running) "Stop" else "Start"

            val launch = ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(
                    ActionBuilders.AndroidActivity.Builder()
                        .setPackageName(packageName)
                        .setClassName("com.example.ui.MicTileActivity")
                        .build()
                )
                .build()

            val clickable = ModifiersBuilders.Clickable.Builder()
                .setOnClick(launch)
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

            val tile = TileBuilders.Tile.Builder()
                .setResourcesVersion("1")
                .setTimeline(timeline)
                .build()

            completer.set(tile)
            "MicTileService#onTileRequest"
        }

    override fun onResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> =
        CallbackToFutureAdapter.getFuture { completer ->
            val res = ResourceBuilders.Resources.Builder()
                .setVersion("1")
                .build()
            completer.set(res)
            "MicTileService#onResourcesRequest"
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
