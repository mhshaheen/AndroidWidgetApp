package com.sm_fs.custommedia3player.core.player

import android.content.Context
import com.sm_fs.custommedia3player.core.config.NotificationConfiguration
import com.sm_fs.custommedia3player.domain.callback.CustomAnalyticsListener
import com.sm_fs.custommedia3player.domain.callback.ErrorAction
import com.sm_fs.custommedia3player.domain.callback.ErrorHandler
import com.sm_fs.custommedia3player.domain.callback.PlayerError
import com.sm_fs.custommedia3player.domain.callback.UrlResolver
import com.sm_fs.custommedia3player.domain.callback.playerConfiguration
import com.sm_fs.custommedia3player.domain.model.PlaybackAnalytics
import com.sm_fs.custommedia3player.domain.model.Track


object MediaPlayerProvider {
    private var instance: GenericMediaPlayer? = null

    fun getInstance(context: Context): GenericMediaPlayer {
        if (instance == null) {
            instance = GenericMediaPlayer.create(
                context = context.applicationContext,
                configuration = playerConfiguration {
                    cacheSizeMb = 500L
                    seekIncrementMs = 10000L
                    userAgent = "MyMusicApp/1.0"
                    enableAnalytics = true

                    urlResolver = UrlResolver { track ->
                        try {
                            val freshUrl = fetchStreamUrlFromApi(track)
                            Result.success(freshUrl)
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    }

                    customAnalyticsListener = CustomAnalyticsListener { analytics ->
                        sendAnalyticsToBackend(analytics)
                    }

                    errorHandler = ErrorHandler { error ->
                        when (error) {
                            is PlayerError.NetworkError -> ErrorAction.RETRY
                            else -> ErrorAction.CONTINUE
                        }
                    }
                },
                notificationConfig = NotificationConfiguration(
                    channelId = "my_music_channel",
                    channelName = "Music Playback",
                    notificationId = 1001,
                    smallIconRes = android.R.drawable.ic_media_play
                )
            )
        }
        return instance!!
    }


    fun fetchStreamUrlFromApi(track: Track): String {
        return "https://coreapi.shadhinmusic.com/api/v5/streaming/getpth?ttype=&name=/${track.playbackUrl}"
    }

    fun sendAnalyticsToBackend(analytics: PlaybackAnalytics) {

    }
}