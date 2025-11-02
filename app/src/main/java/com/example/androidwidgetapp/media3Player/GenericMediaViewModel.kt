//package com.example.androidwidgetapp.media3Player
//
//import android.app.Application
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.ServiceConnection
//import android.os.Build
//import android.os.IBinder
//import android.util.Log
//import androidx.annotation.OptIn
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ProcessLifecycleOwner
//import androidx.lifecycle.asLiveData
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.viewModelScope
//import androidx.media3.common.PlaybackException
//import androidx.media3.common.util.UnstableApi
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//
///**
// * Generic Media ViewModel
// * Can be used in any app by providing configuration and callbacks
// *
// * Usage:
// * val viewModel = GenericMediaViewModel(application)
// * viewModel.configure(config)
// * viewModel.setUrlResolver { track -> Triple(200, "url", null) }
// * viewModel.startPlaying(tracks)
// */
//@OptIn(UnstableApi::class)
//class GenericMediaViewModel(
//    private val app: Application,
//    //private val serviceClass: Class<*> = GenericMediaService::class.java
//) : AndroidViewModel(app) {
//    private val serviceClass = GenericMediaService::class.java
//
//    private var serviceHandler: GenericMediaServiceHandler? = null
//    private val serviceIntent = Intent(app, serviceClass)
//    private var serviceState: GenericServiceState = GenericServiceState.UNKNOWN
//    private var config: GenericMediaConfig = DefaultMediaConfig()
//
//    // URL resolution
//    private var urlResolver: (suspend (GenericTrack) -> Triple<Int?, String?, String?>)? = null
//    private val getUrlResolver: () -> (suspend (GenericTrack) -> Triple<Int?, String?, String?>)? = {
//        urlResolver
//    }
//
//    // Sleep timer
//    private var sleepTimerFunc: ((duration: Long, willStart: Boolean) -> Unit)? = null
//    private val sleepTimerTickCallback = { durationStart: Long, durationLeft: Long, isActive: Boolean ->
//        _sleepTimer.value = Triple(durationStart, durationLeft, isActive)
//    }
//
//    // Service connection
//    private val serviceConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            if (service is GenericMediaService.MediaBinder) {
//                serviceState = GenericServiceState.CONNECTED
//                service.service.urlFetchCallback = getUrlResolver
//                service.service.sleepTimerTickCallback = sleepTimerTickCallback
//                sleepTimerFunc = { duration, willStart ->
//                    if (willStart) {
//                        service.service.setSleepTimer(duration)
//                    } else {
//                        service.service.removeSleepTimer()
//                    }
//                }
//
//                serviceHandler = GenericMediaServiceHandler(
//                    player = service.service.player,
//                    mediaSession = service.service.mediaSession,
//                    coroutineScope = viewModelScope,
//                    config = config
//                )
//
//                service.service.showInitialNotification()
//                pendingStartPlayRequest?.invoke()
//                pendingStartPlayRequest = null
//                setObservers()
//            }
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            serviceState = GenericServiceState.DISCONNECTED
//            pendingStartPlayRequest = null
//            serviceHandler = null
//        }
//    }
//
//    // State flows
//    private val _shuffleModeEnabled = MutableStateFlow(false)
//    private val _playlist = MutableStateFlow<List<GenericTrack>>(emptyList())
//    private val _nowPlaying = MutableStateFlow<GenericTrack?>(null)
//    private val _progress = MutableStateFlow(Triple(0L, 0L, 0))
//    private val _playerState = MutableStateFlow(GenericPlayerState.IDLE)
//    private val _isNextAvailable = MutableStateFlow(false)
//    private val _isPreviousAvailable = MutableStateFlow(false)
//    private val _repeatMode = MutableStateFlow(GenericRepeatMode.NONE)
//    private val _onPlayerError = MutableStateFlow<PlaybackException?>(null)
//    private val _getPlayerError = MutableStateFlow<Triple<Int?, String?, String?>?>(null)
//    private val _sleepTimer = MutableStateFlow(Triple(0L, 0L, false))
//    private val _playerPlaybackSpeed = MutableStateFlow(1.0f)
//    private val _playerAnalytics = MutableStateFlow<GenericAnalytics?>(null)
//
//    // LiveData for UI observation
//    val shuffleModeEnabled: LiveData<Boolean> = _shuffleModeEnabled.asLiveData()
//    val playlist: LiveData<List<GenericTrack>> = _playlist.asLiveData()
//    val nowPlaying: LiveData<GenericTrack?> = _nowPlaying.asLiveData()
//    val progress: LiveData<Triple<Long, Long, Int>> = _progress.asLiveData()
//    val playerState: LiveData<GenericPlayerState> = _playerState.asLiveData()
//    val isNextAvailable: LiveData<Boolean> = _isNextAvailable.asLiveData()
//    val isPreviousAvailable: LiveData<Boolean> = _isPreviousAvailable.asLiveData()
//    val repeatMode: LiveData<GenericRepeatMode> = _repeatMode.asLiveData()
//    val onPlayerError: LiveData<PlaybackException?> = _onPlayerError.asLiveData()
//    val getPlayerError: LiveData<Triple<Int?, String?, String?>?> = _getPlayerError.asLiveData()
//    val sleepTimer: LiveData<Triple<Long, Long, Boolean>> = _sleepTimer.asLiveData()
//    val playerPlaybackSpeed: LiveData<Float> = _playerPlaybackSpeed.asLiveData()
//    val playerAnalytics: LiveData<GenericAnalytics?> = _playerAnalytics.asLiveData()
//
//    // Additional state
//    var playWhenReady: Boolean = true
//    private var pendingStartPlayRequest: (() -> Unit)? = null
//
//    // Configure the player
//    fun configure(config: GenericMediaConfig) {
//        this.config = config
//    }
//
//    // Set URL resolver callback
//    fun setUrlResolver(resolver: suspend (GenericTrack) -> Triple<Int?, String?, String?>) {
//        this.urlResolver = resolver
//    }
//
//    // Set analytics callback (optional)
//    fun setAnalyticsCallback(callback: (GenericAnalytics) -> Unit) {
//        viewModelScope.launch {
//            _playerAnalytics.collectLatest { analytics ->
//                analytics?.let { callback(it) }
//            }
//        }
//    }
//
//    private fun setObservers() {
//        viewModelScope.launch {
//            serviceHandler?.isShuffle?.collect {
//                _shuffleModeEnabled.value = it
//            }
//        }
//        viewModelScope.launch {
//            serviceHandler?.playlist?.collect {
//                _playlist.value = it
//            }
//        }
//        viewModelScope.launch {
//            serviceHandler?.nowPlaying?.collect {
//                _nowPlaying.value = it
//            }
//        }
//        viewModelScope.launch {
//            serviceHandler?.progress?.collect {
//                _progress.value = it
//            }
//        }
//        viewModelScope.launch {
//            serviceHandler?.playerState?.collect {
//                _playerState.value = it
//            }
//        }
//        viewModelScope.launch {
//            serviceHandler?.isNextAvailable?.collect {
//                _isNextAvailable.value = it
//            }
//        }
//        viewModelScope.launch {
//            serviceHandler?.isPreviousAvailable?.collect {
//                _isPreviousAvailable.value = it
//            }
//        }
//        viewModelScope.launch {
//            serviceHandler?.repeatMode?.collect {
//                _repeatMode.value = it
//            }
//        }
//        viewModelScope.launch {
//            serviceHandler?.onPlayerError?.collectLatest {
//                _onPlayerError.value = it
//            }
//        }
//        viewModelScope.launch {
//            serviceHandler?.playerPlayBackSpeed?.collect {
//                _playerPlaybackSpeed.value = it
//            }
//        }
//        viewModelScope.launch {
//            serviceHandler?.playerAnalytics?.collectLatest {
//                _playerAnalytics.value = it
//            }
//        }
//    }
//
//    private fun startMusicService() {
//        if (serviceState !in listOf(GenericServiceState.CONNECTED, GenericServiceState.CONNECTING)) {
//            serviceState = GenericServiceState.CONNECTING
//
//            try {
//                val canStartForeground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
//                } else {
//                    true
//                }
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    if (canStartForeground) {
//                        ContextCompat.startForegroundService(app, serviceIntent)
//                    } else {
//                        app.startService(serviceIntent)
//                        ProcessLifecycleOwner.get().lifecycleScope.launch {
//                            delay(1000)
//                            try {
//                                ContextCompat.startForegroundService(app, serviceIntent)
//                            } catch (ignored: Exception) {
//                                // Service keeps running in background
//                            }
//                        }
//                    }
//                } else {
//                    app.startService(serviceIntent)
//                }
//                app.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                serviceState = GenericServiceState.DISCONNECTED
//            }
//        }
//    }
//
//    fun startPlaying(tracks: List<GenericTrack>, positionToPlay: Int = 0, isShuffled: Boolean = false) {
//        if (serviceState == GenericServiceState.CONNECTED) {
//            performStartPlayback(tracks, positionToPlay, isShuffled)
//        } else {
//            pendingStartPlayRequest = {
//                performStartPlayback(tracks, positionToPlay, isShuffled)
//            }
//            Log.d("shaheen","yes")
//            startMusicService()
//        }
//    }
//
//
//    private fun performStartPlayback(tracks: List<GenericTrack>, positionToPlay: Int, isShuffled: Boolean) {
//        serviceHandler?.startPlaying(tracks, positionToPlay, playWhenReady)
//        serviceHandler?.setShuffle(isShuffled)
//        playWhenReady = true
//    }
//
//    // Playback controls
//    fun toggleShuffle(toggleTo: Boolean? = null) {
//        toggleTo?.let {
//            serviceHandler?.setShuffle(it)
//        } ?: run {
//            serviceHandler?.setShuffle(!_shuffleModeEnabled.value)
//        }
//    }
//
//    fun toggleRepeat() {
//        serviceHandler?.toggleRepeat()
//    }
//
//    fun togglePlayPause() {
//        serviceHandler?.togglePlayPause()
//    }
//
//    fun play() {
//        serviceHandler?.play()
//    }
//
//    fun pause() {
//        serviceHandler?.pause()
//    }
//
//    fun next() {
//        serviceHandler?.next()
//    }
//
//    fun previous() {
//        serviceHandler?.previous()
//    }
//
//    fun setPlaybackSpeed(speed: Float) {
//        serviceHandler?.setPlaybackSpeed(speed)
//    }
//
//    fun seekTo(toMillis: Long) {
//        serviceHandler?.seekTo(toMillis)
//    }
//
//    fun seekToSeconds(toSecs: Int) {
//        serviceHandler?.seekToSeconds(toSecs)
//    }
//
//    fun playItemAt(index: Int) {
//        serviceHandler?.playItemAt(index)
//    }
//
//    fun stop() {
//        serviceHandler?.stop()
//    }
//
//    fun addToQueue(tracks: List<GenericTrack>) = viewModelScope.launch {
//        serviceHandler?.addToQueue(tracks)
//    }
//
//    fun removeItemByPosition(position: Int) {
//        serviceHandler?.removeItemByPosition(position)
//    }
//
//    fun moveMediaItem(fromPosition: Int, toPosition: Int) {
//        serviceHandler?.moveMediaItem(fromPosition, toPosition)
//    }
//
//    // State queries
//    fun isPrepared(): Boolean = serviceHandler?.isPrepared() ?: false
//    fun isPlaying(): Boolean = serviceHandler?.isPlaying() ?: false
//    fun isMediaDataAvailable(): Boolean = playlist.value?.isNotEmpty() ?: false
//    fun nowPlayingPosition(): Int = serviceHandler?.nowPlayingPosition() ?: 0
//
//    fun getPositionById(id: String): Int {
//        val playlist = playlist.value ?: return 0
//        return playlist.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: 0
//    }
//
//    fun isCurrentTrackPlaying(): Boolean {
//        return playlist.value?.any { it == nowPlaying.value } ?: false
//    }
//
//    // Sleep timer
//    fun startSleepTimer(duration: Long) {
//        sleepTimerFunc?.invoke(duration, true)
//    }
//
//    fun stopSleepTimer() {
//        sleepTimerFunc?.invoke(0, false)
//    }
//
//    // Error handling
//    fun resetErrors() {
//        _getPlayerError.value = null
//        _onPlayerError.value = null
//    }
//
//    // Cleanup
//    fun onStop() {
//        try {
//            stop()
//            if (serviceState == GenericServiceState.CONNECTED) {
//                app.unbindService(serviceConnection)
//                serviceState = GenericServiceState.DISCONNECTED
//            }
//            if (!isPlaying() && !isPrepared()) {
//                app.stopService(serviceIntent)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    override fun onCleared() {
//        resetErrors()
//        super.onCleared()
//    }
//}

package com.example.androidwidgetapp.media3Player

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Generic Media ViewModel
 * Can be used in any app by providing configuration and callbacks
 *
 * Usage:
 * val viewModel = GenericMediaViewModel(application)
 * viewModel.configure(config)
 * viewModel.enableUrlInterceptor(false) // for direct URLs
 * viewModel.startPlaying(tracks)
 */
@OptIn(UnstableApi::class)
class GenericMediaViewModel(
    private val app: Application
) : AndroidViewModel(app) {
    private val serviceClass = GenericMediaService::class.java

    private var serviceHandler: GenericMediaServiceHandler? = null
    private val serviceIntent = Intent(app, serviceClass)
    private var serviceState: GenericServiceState = GenericServiceState.UNKNOWN
    private var config: GenericMediaConfig = DefaultMediaConfig()

    // URL resolution
    private var urlResolver: (suspend (GenericTrack) -> Triple<Int?, String?, String?>)? = null
    private val getUrlResolver: () -> (suspend (GenericTrack) -> Triple<Int?, String?, String?>)? = {
        urlResolver
    }

    // Sleep timer
    private var sleepTimerFunc: ((duration: Long, willStart: Boolean) -> Unit)? = null
    private val sleepTimerTickCallback = { durationStart: Long, durationLeft: Long, isActive: Boolean ->
        _sleepTimer.value = Triple(durationStart, durationLeft, isActive)
    }

    // Interceptor control
    private var interceptorEnabled: Boolean = false

    // Service connection
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("PERF_TEST", "=== SERVICE CONNECTED ===")
            if (service is GenericMediaService.MediaBinder) {
                serviceState = GenericServiceState.CONNECTED

                // Set interceptor state BEFORE setting callbacks
                service.service.setInterceptorEnabled(interceptorEnabled)

                service.service.urlFetchCallback = getUrlResolver
                service.service.sleepTimerTickCallback = sleepTimerTickCallback
                sleepTimerFunc = { duration, willStart ->
                    if (willStart) {
                        service.service.setSleepTimer(duration)
                    } else {
                        service.service.removeSleepTimer()
                    }
                }

                serviceHandler = GenericMediaServiceHandler(
                    player = service.service.player,
                    mediaSession = service.service.mediaSession,
                    coroutineScope = viewModelScope,
                    config = config
                )

                service.service.showInitialNotification()
                pendingStartPlayRequest?.invoke()
                pendingStartPlayRequest = null
                setObservers()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceState = GenericServiceState.DISCONNECTED
            pendingStartPlayRequest = null
            serviceHandler = null
        }
    }

    // State flows
    private val _shuffleModeEnabled = MutableStateFlow(false)
    private val _playlist = MutableStateFlow<List<GenericTrack>>(emptyList())
    private val _nowPlaying = MutableStateFlow<GenericTrack?>(null)
    private val _progress = MutableStateFlow(Triple(0L, 0L, 0))
    private val _playerState = MutableStateFlow(GenericPlayerState.IDLE)
    private val _isNextAvailable = MutableStateFlow(false)
    private val _isPreviousAvailable = MutableStateFlow(false)
    private val _repeatMode = MutableStateFlow(GenericRepeatMode.NONE)
    private val _onPlayerError = MutableStateFlow<PlaybackException?>(null)
    private val _getPlayerError = MutableStateFlow<Triple<Int?, String?, String?>?>(null)
    private val _sleepTimer = MutableStateFlow(Triple(0L, 0L, false))
    private val _playerPlaybackSpeed = MutableStateFlow(1.0f)
    private val _playerAnalytics = MutableStateFlow<GenericAnalytics?>(null)

    // LiveData for UI observation
    val shuffleModeEnabled: LiveData<Boolean> = _shuffleModeEnabled.asLiveData()
    val playlist: LiveData<List<GenericTrack>> = _playlist.asLiveData()
    val nowPlaying: LiveData<GenericTrack?> = _nowPlaying.asLiveData()
    val progress: LiveData<Triple<Long, Long, Int>> = _progress.asLiveData()
    val playerState: LiveData<GenericPlayerState> = _playerState.asLiveData()
    val isNextAvailable: LiveData<Boolean> = _isNextAvailable.asLiveData()
    val isPreviousAvailable: LiveData<Boolean> = _isPreviousAvailable.asLiveData()
    val repeatMode: LiveData<GenericRepeatMode> = _repeatMode.asLiveData()
    val onPlayerError: LiveData<PlaybackException?> = _onPlayerError.asLiveData()
    val getPlayerError: LiveData<Triple<Int?, String?, String?>?> = _getPlayerError.asLiveData()
    val sleepTimer: LiveData<Triple<Long, Long, Boolean>> = _sleepTimer.asLiveData()
    val playerPlaybackSpeed: LiveData<Float> = _playerPlaybackSpeed.asLiveData()
    val playerAnalytics: LiveData<GenericAnalytics?> = _playerAnalytics.asLiveData()

    // Additional state
    var playWhenReady: Boolean = true
    private var pendingStartPlayRequest: (() -> Unit)? = null

    // Configure the player
    fun configure(config: GenericMediaConfig) {
        this.config = config
    }

    /**
     * Enable or disable URL interceptor
     *
     * @param enabled true if you need to intercept and fetch URLs dynamically
     *                false if you have direct URLs (default for better performance)
     *
     * Usage:
     * - For direct URLs: enableUrlInterceptor(false) <- RECOMMENDED for most cases
     * - For dynamic URLs: enableUrlInterceptor(true) + setUrlResolver { ... }
     */
    fun enableUrlInterceptor(enabled: Boolean) {
        this.interceptorEnabled = enabled
    }

    // Set URL resolver callback (only needed if interceptor is enabled)
    fun setUrlResolver(resolver: suspend (GenericTrack) -> Triple<Int?, String?, String?>) {
        this.urlResolver = resolver
        // Auto-enable interceptor when resolver is set
        this.interceptorEnabled = true
    }

    // Set analytics callback (optional)
    fun setAnalyticsCallback(callback: (GenericAnalytics) -> Unit) {
        viewModelScope.launch {
            _playerAnalytics.collectLatest { analytics ->
                analytics?.let { callback(it) }
            }
        }
    }

    private fun setObservers() {
        viewModelScope.launch {
            serviceHandler?.isShuffle?.collect {
                _shuffleModeEnabled.value = it
            }
        }
        viewModelScope.launch {
            serviceHandler?.playlist?.collect {
                _playlist.value = it
            }
        }
        viewModelScope.launch {
            serviceHandler?.nowPlaying?.collect {
                _nowPlaying.value = it
            }
        }
        viewModelScope.launch {
            serviceHandler?.progress?.collect {
                _progress.value = it
            }
        }
        viewModelScope.launch {
            serviceHandler?.playerState?.collect {
                _playerState.value = it
            }
        }
        viewModelScope.launch {
            serviceHandler?.isNextAvailable?.collect {
                _isNextAvailable.value = it
            }
        }
        viewModelScope.launch {
            serviceHandler?.isPreviousAvailable?.collect {
                _isPreviousAvailable.value = it
            }
        }
        viewModelScope.launch {
            serviceHandler?.repeatMode?.collect {
                _repeatMode.value = it
            }
        }
        viewModelScope.launch {
            serviceHandler?.onPlayerError?.collectLatest {
                _onPlayerError.value = it
            }
        }
        viewModelScope.launch {
            serviceHandler?.playerPlayBackSpeed?.collect {
                _playerPlaybackSpeed.value = it
            }
        }
        viewModelScope.launch {
            serviceHandler?.playerAnalytics?.collectLatest {
                _playerAnalytics.value = it
            }
        }
    }

    private fun startMusicService() {
        if (serviceState !in listOf(GenericServiceState.CONNECTED, GenericServiceState.CONNECTING)) {
            serviceState = GenericServiceState.CONNECTING

            try {
                val canStartForeground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                } else {
                    true
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (canStartForeground) {
                        ContextCompat.startForegroundService(app, serviceIntent)
                    } else {
                        app.startService(serviceIntent)
                        ProcessLifecycleOwner.get().lifecycleScope.launch {
                            delay(1000)
                            try {
                                ContextCompat.startForegroundService(app, serviceIntent)
                            } catch (ignored: Exception) {
                                // Service keeps running in background
                            }
                        }
                    }
                } else {
                    app.startService(serviceIntent)
                }
                app.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            } catch (e: Exception) {
                e.printStackTrace()
                serviceState = GenericServiceState.DISCONNECTED
            }
        }
    }

    fun startPlaying(tracks: List<GenericTrack>, positionToPlay: Int = 0, isShuffled: Boolean = false) {
        if (serviceState == GenericServiceState.CONNECTED) {
            performStartPlayback(tracks, positionToPlay, isShuffled)
        } else {
            pendingStartPlayRequest = {
                performStartPlayback(tracks, positionToPlay, isShuffled)
            }
            startMusicService()
        }
    }

    private fun performStartPlayback(tracks: List<GenericTrack>, positionToPlay: Int, isShuffled: Boolean) {
        serviceHandler?.startPlaying(tracks, positionToPlay, playWhenReady)
        serviceHandler?.setShuffle(isShuffled)
        playWhenReady = true
    }

    // Playback controls
    fun toggleShuffle(toggleTo: Boolean? = null) {
        toggleTo?.let {
            serviceHandler?.setShuffle(it)
        } ?: run {
            serviceHandler?.setShuffle(!_shuffleModeEnabled.value)
        }
    }

    fun toggleRepeat() {
        serviceHandler?.toggleRepeat()
    }

    fun togglePlayPause() {
        serviceHandler?.togglePlayPause()
    }

    fun play() {
        serviceHandler?.play()
    }

    fun pause() {
        serviceHandler?.pause()
    }

    fun next() {
        serviceHandler?.next()
    }

    fun previous() {
        serviceHandler?.previous()
    }

    fun setPlaybackSpeed(speed: Float) {
        serviceHandler?.setPlaybackSpeed(speed)
    }

    fun seekTo(toMillis: Long) {
        serviceHandler?.seekTo(toMillis)
    }

    fun seekToSeconds(toSecs: Int) {
        serviceHandler?.seekToSeconds(toSecs)
    }

    fun playItemAt(index: Int) {
        serviceHandler?.playItemAt(index)
    }

    fun stop() {
        serviceHandler?.stop()
    }

    fun addToQueue(tracks: List<GenericTrack>) = viewModelScope.launch {
        serviceHandler?.addToQueue(tracks)
    }

    fun removeItemByPosition(position: Int) {
        serviceHandler?.removeItemByPosition(position)
    }

    fun moveMediaItem(fromPosition: Int, toPosition: Int) {
        serviceHandler?.moveMediaItem(fromPosition, toPosition)
    }

    // State queries
    fun isPrepared(): Boolean = serviceHandler?.isPrepared() ?: false
    fun isPlaying(): Boolean = serviceHandler?.isPlaying() ?: false
    fun isMediaDataAvailable(): Boolean = playlist.value?.isNotEmpty() ?: false
    fun nowPlayingPosition(): Int = serviceHandler?.nowPlayingPosition() ?: 0

    fun getPositionById(id: String): Int {
        val playlist = playlist.value ?: return 0
        return playlist.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: 0
    }

    fun isCurrentTrackPlaying(): Boolean {
        return playlist.value?.any { it == nowPlaying.value } ?: false
    }

    // Sleep timer
    fun startSleepTimer(duration: Long) {
        sleepTimerFunc?.invoke(duration, true)
    }

    fun stopSleepTimer() {
        sleepTimerFunc?.invoke(0, false)
    }

    // Error handling
    fun resetErrors() {
        _getPlayerError.value = null
        _onPlayerError.value = null
    }

    // Cleanup
    fun onStop() {
        try {
            stop()
            if (serviceState == GenericServiceState.CONNECTED) {
                app.unbindService(serviceConnection)
                serviceState = GenericServiceState.DISCONNECTED
            }
            if (!isPlaying() && !isPrepared()) {
                app.stopService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        resetErrors()
        super.onCleared()
    }
}