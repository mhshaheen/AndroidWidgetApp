package com.example.androidwidgetapp.media3Player

import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

class GenericMediaSessionCallback : MediaLibraryService.MediaLibrarySession.Callback {
    
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val sessionCommands = connectionResult.availableSessionCommands
            .buildUpon()
            .build()
        
        return MediaSession.ConnectionResult.accept(
            sessionCommands, 
            connectionResult.availablePlayerCommands
        )
    }
}