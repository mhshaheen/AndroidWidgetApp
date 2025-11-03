package com.sm_fs.custommedia3player.core.player

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import com.sm_fs.custommedia3player.core.config.NotificationConfiguration
import com.sm_fs.custommedia3player.core.service.GenericMediaService
import com.sm_fs.custommedia3player.core.service.MediaServiceConnectionHandler
import com.sm_fs.custommedia3player.domain.callback.PlayerConfiguration
import com.sm_fs.custommedia3player.domain.model.PlaybackProgress
import com.sm_fs.custommedia3player.domain.model.PlayerState
import com.sm_fs.custommedia3player.domain.model.RepeatMode
import com.sm_fs.custommedia3player.domain.model.ShuffleMode
import com.sm_fs.custommedia3player.domain.model.Track
import com.sm_fs.custommedia3player.util.extensions.toMediaItem
import com.sm_fs.custommedia3player.util.extensions.toTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
internal class GenericMediaPlayerImpl(
    private val context: Context,
    private val configuration: PlayerConfiguration,
    private val notificationConfig: NotificationConfiguration
) : GenericMediaPlayer, Player.Listener {
    
    companion object {
        private const val TAG = "GenericMediaPlayer"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var service: GenericMediaService? = null
    private var player: Player? = null
    
    private var progressUpdateJob: Job? = null
    
    // State flows
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    private val _currentTrack = MutableStateFlow<Track?>(null)
    override val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()
    
    private val _playlist = MutableStateFlow<List<Track>>(emptyList())
    override val playlist: StateFlow<List<Track>> = _playlist.asStateFlow()
    
    private val _playbackProgress = MutableStateFlow(PlaybackProgress(0L, 0L, 0))
    override val playbackProgress: StateFlow<PlaybackProgress> = _playbackProgress.asStateFlow()
    
    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.Off)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    private val _shuffleMode = MutableStateFlow(ShuffleMode(false))
    override val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode.asStateFlow()
    
    private val _playbackSpeed = MutableStateFlow(1.0f)
    override val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    
    private val _sleepTimerRemaining = MutableStateFlow(0L)
    override val sleepTimerRemaining: StateFlow<Long> = _sleepTimerRemaining.asStateFlow()
    
    private val serviceConnection = MediaServiceConnectionHandler(
        onConnected = { connectedService ->
            service = connectedService
            connectedService.initialize(configuration, notificationConfig)
            player = connectedService.player
            connectedService.showInitialNotification()
            connectedService.player.addListener(this)
            connectedService.getCurrentTrack = { _currentTrack.value }
            connectedService.onSleepTimerTick = { remaining ->
                _sleepTimerRemaining.value = remaining
            }
            updatePlaylist()
            updateCurrentTrack()
            onRepeatModeChanged(connectedService.player.repeatMode)
            onShuffleModeEnabledChanged(connectedService.player.shuffleModeEnabled)
        },
        onDisconnected = {
            Log.d(TAG, "Service disconnected")
            service = null
            player = null
        }
    )
    
    init {
        startService()
    }

    private fun startService() {
        val intent = Intent(context, GenericMediaService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "Service start and bind initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
        }
    }
    
    // ========== Playback Control ==========
    
    override fun play() {
        player?.play()
    }
    
    override fun pause() {
        player?.pause()
    }
    
    override fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) pause() else play()
        }
    }
    
    override fun next() {
        player?.seekToNext()
    }
    
    override fun previous() {
        player?.seekToPrevious()
    }
    
    override fun stop() {
        player?.stop()
        player?.clearMediaItems()
        updatePlaylist()
    }
    
    override fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }
    
    override fun setPlaybackSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
        _playbackSpeed.value = speed
    }
    
    // ========== Queue Management ==========

    override fun setPlaylist(tracks: List<Track>, startIndex: Int, startPositionMs: Long) {
        Log.d(TAG, "=== setPlaylist called ===")
        Log.d(TAG, "Tracks: ${tracks.size}, startIndex: $startIndex")

        player?.let {
            val mediaItems = tracks.map { track ->
                Log.d(TAG, "Converting track: ${track.title}")
                track.toMediaItem()
            }

            Log.d(TAG, "MediaItems created: ${mediaItems.size}")
            it.setMediaItems(mediaItems, startIndex, startPositionMs)
            it.prepare()
            Log.d(TAG, "Player prepared")
            it.playWhenReady = false
            Log.d(TAG, "Play called")
        } ?: Log.e(TAG, "Player is null!")
    }
    
    override fun addToQueue(tracks: List<Track>) {
        player?.let {
            val mediaItems = tracks.map { track -> track.toMediaItem() }
            it.addMediaItems(mediaItems)
        }
    }
    
    override fun removeFromQueue(index: Int) {
        player?.removeMediaItem(index)
    }
    
    override fun moveInQueue(fromIndex: Int, toIndex: Int) {
        player?.moveMediaItem(fromIndex, toIndex)
    }
    
    override fun clearQueue() {
        player?.clearMediaItems()
    }
    
    override fun playTrackAt(index: Int) {
        player?.let {
            if (index != it.currentMediaItemIndex) {
                it.seekTo(index, 0L)
                it.play()
            }
        }
    }
    
    // ========== Mode Control ==========
    
    override fun setRepeatMode(mode: RepeatMode) {
        player?.repeatMode = mode.toExoPlayerMode()
    }
    
    override fun toggleRepeatMode() {
        val newMode = when (_repeatMode.value) {
            RepeatMode.Off -> RepeatMode.One
            RepeatMode.One -> RepeatMode.All
            RepeatMode.All -> RepeatMode.Off
        }
        setRepeatMode(newMode)
    }
    
    override fun setShuffleMode(enabled: Boolean) {
        player?.shuffleModeEnabled = enabled
    }
    
    override fun toggleShuffle() {
        setShuffleMode(!_shuffleMode.value.enabled)
    }
    
    // ========== Advanced Features ==========
    
    override fun setSleepTimer(durationMs: Long) {
        service?.setSleepTimer(durationMs)
    }
    
    override fun cancelSleepTimer() {
        service?.cancelSleepTimer()
    }
    
    // ========== Query ==========
    
    override fun isPlaying(): Boolean = player?.isPlaying ?: false
    
    override fun getCurrentPosition(): Int = player?.currentMediaItemIndex ?: 0
    
    // ========== Player.Listener Implementation ==========

    override fun onPlaybackStateChanged(playbackState: Int) {
        val stateName = when (playbackState) {
            Player.STATE_IDLE -> "IDLE"
            Player.STATE_BUFFERING -> "BUFFERING"
            Player.STATE_READY -> "READY"
            Player.STATE_ENDED -> "ENDED"
            else -> "UNKNOWN"
        }
        Log.d(TAG, "▶ Playback state changed: $stateName")

        when (playbackState) {
            Player.STATE_IDLE -> _playerState.value = PlayerState.Idle
            Player.STATE_BUFFERING -> _playerState.value = PlayerState.Buffering
            Player.STATE_READY -> _playerState.value = PlayerState.Ready
            Player.STATE_ENDED -> {
                _playerState.value = PlayerState.Ended
                player?.seekTo(0, 0L)
                player?.pause()
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d(TAG, "▶ Is playing changed: $isPlaying")
        Log.d(TAG, "▶ Current track: ${_currentTrack.value?.title}")

        if (isPlaying) {
            _currentTrack.value?.let { track ->
                _playerState.value = PlayerState.Playing(track)
                Log.d(TAG, "▶ Now playing: ${track.title}")
            }
            startProgressUpdates()
        } else {
            _currentTrack.value?.let { track ->
                _playerState.value = PlayerState.Paused(track)
            }
            stopProgressUpdates()
        }
    }
    
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        updateCurrentTrack()
        updatePlaylist()
    }
    
    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
            updatePlaylist()
        }
    }
    
    override fun onRepeatModeChanged(repeatMode: Int) {
        _repeatMode.value = RepeatMode.fromExoPlayerMode(repeatMode)
    }
    
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        _shuffleMode.value = ShuffleMode(shuffleModeEnabled)
    }
    
    override fun onPlayerError(error: PlaybackException) {
        Log.e(TAG, "Player error: ${error.message}", error)
        _playerState.value = PlayerState.Error(error)
    }
    
    // ========== Helper Methods ==========
    
    private fun updateCurrentTrack() {
        _currentTrack.value = player?.currentMediaItem?.toTrack()
    }
    
    private fun updatePlaylist() {
        player?.let { p ->
            val tracks = mutableListOf<Track>()
            for (i in 0 until p.mediaItemCount) {
                p.getMediaItemAt(i).toTrack()?.let { tracks.add(it) }
            }
            _playlist.value = tracks
        }
    }
    
    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = scope.launch {
            while (isActive) {
                updateProgress()
                delay(300)
            }
        }
    }
    
    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }
    
    private fun updateProgress() {
        player?.let {
            _playbackProgress.value = PlaybackProgress(
                currentPositionMs = it.currentPosition,
                durationMs = it.duration.coerceAtLeast(0L),
                bufferedPercentage = it.bufferedPercentage
            )
        }
    }
    
    // ========== Lifecycle ==========
    
    override fun release() {
        stopProgressUpdates()
        scope.cancel()
        player?.removeListener(this)
        context.unbindService(serviceConnection)
        service = null
        player = null
        Log.d(TAG, "Player released")
    }
}