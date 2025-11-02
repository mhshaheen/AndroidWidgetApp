package com.example.androidwidgetapp.media3Player

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.session.MediaSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Generic Media Service Handler
 * Manages playback state and exposes reactive streams for UI updates
 */
@OptIn(UnstableApi::class)
class GenericMediaServiceHandler(
    private val player: ExoPlayer,
    private val mediaSession: MediaSession,
    private val coroutineScope: CoroutineScope,
    private val config: GenericMediaConfig = DefaultMediaConfig()
) : Player.Listener {

    private val progressJob = Job()
    private val SKIP_THRESHOLD_MS = 5000L
    private var duration: Long = 0L
    private var currentCursorPosition: Long = 0L
    private var isSkipped: Boolean = false

    // Reactive state flows
    val isShuffle = MutableStateFlow(false)
    val repeatMode = MutableStateFlow(GenericRepeatMode.NONE)
    val playlist = MutableStateFlow<List<GenericTrack>>(emptyList())
    val nowPlaying = MutableStateFlow<GenericTrack?>(player.currentMediaItem?.getGenericTrack())
    val progress = MutableStateFlow(Triple(0L, 0L, 0))
    val playerState = MutableStateFlow(GenericPlayerState.IDLE)
    val isNextAvailable = MutableStateFlow(false)
    val isPreviousAvailable = MutableStateFlow(false)
    val onPlayerError = MutableStateFlow<PlaybackException?>(null)
    val playerPlayBackSpeed = MutableStateFlow(1.0f)
    val playerAnalytics = MutableStateFlow<GenericAnalytics?>(null)

    init {
        player.addListener(this)
        player.addAnalyticsListener(TrackPlaybackAnalytics())
        onShuffleModeEnabledChanged(player.shuffleModeEnabled)
        updatePlaylist()
        onPlaybackStateChanged(player.playbackState)
        onIsPlayingChanged(player.isPlaying)
        updateNextPreviousTrackAvailability()
        onRepeatModeChanged(player.repeatMode)
    }

    fun startPlaying(
        tracks: List<GenericTrack>,
        positionToPlay: Int = 0,
        playWhenReady: Boolean = true
    ) {
        coroutineScope.launch {
            val startPositionMs = (tracks[positionToPlay].currentDurationCursor * 1000)
            player.setMediaItems(tracks.map { it.toMediaItem() }, positionToPlay, startPositionMs)
            setPlaybackSpeed(1f)
            player.prepare()
            player.playWhenReady = playWhenReady
        }
    }

    fun setShuffle(shuffle: Boolean) {
        player.shuffleModeEnabled = shuffle
    }

    fun toggleRepeat() {
        when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> player.repeatMode = Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> player.repeatMode = Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> player.repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
                player.playWhenReady = true
            }
            player.play()
        }
    }

    fun play() = player.play()
    fun pause() = player.pause()
    fun next() = player.seekToNext()
    fun previous() = player.seekToPrevious()

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        playerPlayBackSpeed.value = speed
    }

    fun seekTo(toMillis: Long) {
        player.seekTo(toMillis)
    }

    fun seekToSeconds(toSecs: Int) {
        player.seekTo(toSecs * 1000L)
    }

    fun playItemAt(index: Int) {
        if (index == player.currentMediaItemIndex) return
        player.seekTo(index, 0L)
        player.play()
    }

    fun stop() {
        player.pause()
        player.clearMediaItems()
        player.stop()
        updatePlaylist()
    }

    fun addToQueue(tracks: List<GenericTrack>) {
        val tracksToAdd = tracks.subtract(playlist.value.toSet())
        player.addMediaItems(tracksToAdd.map { it.toMediaItem() })
    }

    fun removeItemByPosition(position: Int) {
        player.removeMediaItem(position)
    }

    fun moveMediaItem(fromPosition: Int, toPosition: Int) {
        player.moveMediaItem(fromPosition, toPosition)
    }

    fun isPrepared(): Boolean = player.playWhenReady
    fun isPlaying(): Boolean = player.isPlaying
    fun nowPlayingPosition(): Int = player.currentMediaItemIndex

    private fun updateNextPreviousTrackAvailability() {
        isNextAvailable.value = player.hasNextMediaItem()
        isPreviousAvailable.value = player.hasPreviousMediaItem()
    }

    private fun updatePlaylist() {
        val array = ArrayList<GenericTrack>()
        for (index in 0 until player.mediaItemCount) {
            player.getMediaItemAt(index).getGenericTrack()?.copy()?.let {
                it.isCurrentlyPlaying = it.id == nowPlaying.value?.id
                array.add(it)
            }
        }
        playlist.value = array
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        isShuffle.value = shuffleModeEnabled
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        when (repeatMode) {
            Player.REPEAT_MODE_OFF -> this.repeatMode.value = GenericRepeatMode.NONE
            Player.REPEAT_MODE_ONE -> this.repeatMode.value = GenericRepeatMode.ONE
            Player.REPEAT_MODE_ALL -> this.repeatMode.value = GenericRepeatMode.ALL
        }
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
            updatePlaylist()
            updateNextPreviousTrackAvailability()
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        error.localizedMessage?.let {
            android.util.Log.e("GenericMediaHandler", "onPlayerError: $it")
        }
        error.printStackTrace()
        onPlayerError.value = error
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        nowPlaying.value = mediaItem?.getGenericTrack()
        setPlaybackSpeed(1f)
        updatePlaylist()
        updateNextPreviousTrackAvailability()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            playerState.value = GenericPlayerState.PLAYING
            playerPlayBackSpeed.value = player.playbackParameters.speed
            coroutineScope.launch {
                progressJob.cancel()
                progressJob.run {
                    while (true) {
                        delay(300)
                        updatePlayerProgressUi()
                    }
                }
            }
        } else {
            playerState.value = GenericPlayerState.NOT_PLAYING
            progressJob.cancel()
        }
    }

    private fun updatePlayerProgressUi() {
        duration = player.duration
        currentCursorPosition = player.currentPosition
        val bufferedPercentage = player.bufferedPercentage
        progress.value = Triple(
            currentCursorPosition,
            duration,
            bufferedPercentage
        )
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_IDLE -> {
                Log.d("PERF_TEST", "Player: IDLE")
                playerState.value = GenericPlayerState.IDLE
            }
            ExoPlayer.STATE_ENDED -> {
                Log.d("PERF_TEST", "Player: ENDED")
                playerState.value = GenericPlayerState.ENDED
                player.seekTo(0, 0L)
                player.pause()
            }
            ExoPlayer.STATE_BUFFERING -> {
                Log.d("PERF_TEST", "Player: BUFFERING")
                playerState.value = GenericPlayerState.BUFFERING
            }
            ExoPlayer.STATE_READY -> {
                Log.d("PERF_TEST", "Player: READY")
                playerState.value = GenericPlayerState.READY
                updatePlayerProgressUi()
            }
        }
    }

    @UnstableApi
    inner class TrackPlaybackAnalytics : AnalyticsListener {
        private var analytics: GenericAnalytics? = null

        override fun onEvents(player: Player, events: AnalyticsListener.Events) {
            if (events.contains(AnalyticsListener.EVENT_IS_PLAYING_CHANGED)) {
                handlePlayingChanged(player)
            }
            if (events.contains(AnalyticsListener.EVENT_POSITION_DISCONTINUITY)) {
                handlePositionDiscontinuity(player)
            }
        }

        private fun handlePlayingChanged(player: Player) {
            if (player.isPlaying) {
                if (analytics == null) {
                    player.currentMediaItem?.getGenericTrack()?.let {
                        analytics = GenericAnalytics(
                            track = it,
                            playedMS = 0,
                            bufferingMS = 0,
                            startTime = System.currentTimeMillis(),
                            lastPlayedMS = System.currentTimeMillis(),
                            currentCursorPosition = currentCursorPosition,
                            isSkipped = isSkipped,
                            duration = duration
                        )
                    }
                }
                analytics?.lastPlayedMS = System.currentTimeMillis()
            } else {
                analytics?.let {
                    if (it.lastPlayedMS != 0L) {
                        it.playedMS += System.currentTimeMillis() - it.lastPlayedMS
                        it.lastPlayedMS = 0
                    }
                }
            }
        }

        private fun handlePositionDiscontinuity(player: Player) {
            analytics?.let {
                if (it.lastPlayedMS != 0L) {
                    it.playedMS += System.currentTimeMillis() - it.lastPlayedMS
                    it.lastPlayedMS = 0
                }

                if (player.currentPosition - currentCursorPosition > SKIP_THRESHOLD_MS) {
                    isSkipped = true
                }

                if (player.currentMediaItem?.mediaId != it.track.id) {
                    it.endTime = System.currentTimeMillis()
                    it.currentCursorPosition = currentCursorPosition
                    it.isSkipped = isSkipped
                    it.duration = duration

                    if (playerAnalytics.value?.track?.id != it.track.id) {
                        playerAnalytics.value = it
                    }

                    player.currentMediaItem?.getGenericTrack()?.let { currentTrack ->
                        analytics = GenericAnalytics(
                            track = currentTrack,
                            playedMS = 0,
                            bufferingMS = 0,
                            startTime = System.currentTimeMillis(),
                            lastPlayedMS = 0L,
                            currentCursorPosition = currentCursorPosition,
                            isSkipped = isSkipped,
                            duration = duration
                        )
                    } ?: run { analytics = null }
                }

                isSkipped = false
            }

            currentCursorPosition = player.currentPosition
        }
    }
}