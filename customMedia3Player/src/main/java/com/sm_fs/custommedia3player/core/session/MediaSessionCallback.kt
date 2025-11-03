package com.sm_fs.custommedia3player.core.session

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@OptIn(UnstableApi::class)
class MediaSessionCallback : MediaLibraryService.MediaLibrarySession.Callback {
    
    companion object {
        private const val TAG = "MediaSessionCallback"
    }
    
    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<androidx.media3.common.MediaItem>
    ): ListenableFuture<MutableList<androidx.media3.common.MediaItem>> {
        Log.d(TAG, "onAddMediaItems: ${mediaItems.size} items")
        return Futures.immediateFuture(mediaItems)
    }

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val sessionCommands =
            connectionResult.availableSessionCommands
                .buildUpon()
                // Add custom commands
                //.add(SessionCommand(MEDIA_CUSTOM_COMMAND.LIKE, Bundle()))
                //.add(SessionCommand(MEDIA_CUSTOM_COMMAND.REPEAT, Bundle()))
                .build()
        return MediaSession.ConnectionResult.accept(
            sessionCommands,
            connectionResult.availablePlayerCommands
        )
    }
}