//package com.example.androidwidgetapp.media3Player
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.content.pm.ServiceInfo
//import android.os.Binder
//import android.os.Build
//import android.os.CountDownTimer
//import android.os.IBinder
//import androidx.core.app.NotificationCompat
//import androidx.media3.common.AudioAttributes
//import androidx.media3.common.C
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.database.StandaloneDatabaseProvider
//import androidx.media3.datasource.DefaultDataSource
//import androidx.media3.datasource.cache.CacheDataSource
//import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
//import androidx.media3.datasource.cache.SimpleCache
//import androidx.media3.datasource.okhttp.OkHttpDataSource
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
//import androidx.media3.extractor.ExtractorsFactory
//import androidx.media3.extractor.mp3.Mp3Extractor
//import androidx.media3.session.DefaultMediaNotificationProvider
//import androidx.media3.session.MediaController
//import androidx.media3.session.MediaLibraryService
//import androidx.media3.session.MediaSession
//import androidx.media3.session.SessionToken
//import com.google.common.util.concurrent.MoreExecutors
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import okhttp3.Protocol
//import okhttp3.Request
//import okhttp3.Response
//import okhttp3.ResponseBody.Companion.toResponseBody
//import java.io.File
//import android.content.ComponentName
//import androidx.core.net.toUri
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.runBlocking
//
///**
// * Generic Media3 Service that can be configured for any app
// *
// * Usage:
// * 1. Create a configuration object implementing GenericMediaConfig
// * 2. Pass the config when binding to the service
// * 3. Use GenericMediaServiceHandler to control playback
// */
//@UnstableApi
//class GenericMediaService : MediaLibraryService() {
//
//    lateinit var cache: SimpleCache private set
//    lateinit var player: ExoPlayer private set
//    private var countDownTimer: CountDownTimer? = null
//    private var sleepDurationStart: Long = -1
//
//    // Callbacks
//    var sleepTimerTickCallback: ((durationStart: Long, durationLeft: Long, isActive: Boolean) -> Unit)? = null
//    var urlFetchCallback: (() -> (suspend (GenericTrack) -> Triple<Int?, String?, String?>)?)? = null
//
//    lateinit var mediaSession: MediaLibrarySession private set
//    lateinit var mediaSessionCallback: GenericMediaSessionCallback private set
//    lateinit var notificationProvider: DefaultMediaNotificationProvider private set
//
//    // Configuration
//    private var config: GenericMediaConfig = DefaultMediaConfig()
//
//    companion object {
//        const val DEFAULT_NOTIFICATION_CHANNEL_ID = "generic_media_notification"
//        const val DEFAULT_NOTIFICATION_ID = 1001
//        const val DEFAULT_NOTIFICATION_NAME = "Media"
//        const val DEFAULT_NOTIFICATION_DESCRIPTION = "Media Playback"
//    }
//
//    private val binder = MediaBinder()
//
//    override fun onBind(intent: Intent?): IBinder =
//        super.onBind(intent) ?: binder
//
//    override fun onCreate() {
//        super.onCreate()
//
//        createNotificationChannel(this)
//
//        player = ExoPlayer.Builder(this)
//            .setAudioAttributes(provideAudioAttributes(), true)
//            .setWakeMode(C.WAKE_MODE_NETWORK)
//            .setHandleAudioBecomingNoisy(true)
//            .setSeekForwardIncrementMs(config.seekForwardMs)
//            .setSeekBackIncrementMs(config.seekBackwardMs)
//            .setMediaSourceFactory(getMediaSourceFactoryWithCache())
//            .build()
//
//        mediaSessionCallback = GenericMediaSessionCallback()
//        mediaSession = provideMediaLibrarySession(
//            this,
//            this,
//            player,
//            mediaSessionCallback
//        )
//
//        try {
//            val sessionToken = SessionToken(this, ComponentName(this, GenericMediaService::class.java))
//            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
//            controllerFuture.addListener({ controllerFuture.get() }, MoreExecutors.directExecutor())
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        notificationProvider = DefaultMediaNotificationProvider.Builder(this)
//            .setNotificationId(config.notificationId)
//            .setChannelId(config.notificationChannelId)
//            .setChannelName(config.notificationChannelNameRes)
//            .build().apply {
//                config.notificationIconRes?.let { setSmallIcon(it) }
//            }
//
//        setMediaNotificationProvider(notificationProvider)
//    }
//
//    private fun createNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                config.notificationChannelId,
//                config.notificationChannelName,
//                NotificationManager.IMPORTANCE_LOW
//            )
//            channel.description = config.notificationChannelDescription
//            val manager = context.getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//    }
//
//    fun showInitialNotification() {
//        val notification = buildPlaceholderNotification()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            startForeground(config.notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
//        } else {
//            startForeground(config.notificationId, notification)
//        }
//    }
//
//    private fun buildPlaceholderNotification(): Notification {
//        return NotificationCompat.Builder(this, config.notificationChannelId)
//            .setContentTitle(config.appName)
//            .setContentText("Preparing to play...")
//            .setSmallIcon(config.notificationIconRes ?: android.R.drawable.ic_media_play)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setOnlyAlertOnce(true)
//            .build()
//    }
//
//    private fun provideAudioAttributes(): AudioAttributes =
//        AudioAttributes.Builder()
//            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
//            .setUsage(C.USAGE_MEDIA)
//            .build()
//
//    private fun provideMediaLibrarySession(
//        context: Context,
//        service: MediaLibraryService,
//        player: ExoPlayer,
//        callback: GenericMediaSessionCallback,
//    ): MediaLibrarySession {
//        val track = player.currentMediaItem?.getGenericTrack()
//        val deepLinkUri = config.deepLinkProvider?.invoke(track)?.toUri()
//
//        val pendingIntent = deepLinkUri?.let { uri ->
//            val sessionIntent = Intent(Intent.ACTION_VIEW, uri).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                `package` = context.packageName
//            }
//            PendingIntent.getActivity(
//                context,
//                0,
//                sessionIntent,
//                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            )
//        }
//
//        val builder = MediaLibrarySession.Builder(service, player, callback)
//        pendingIntent?.let { builder.setSessionActivity(it) }
//        config.bitmapLoader?.let { builder.setBitmapLoader(it) }
//
//        return builder.build()
//    }
//
//    private fun getMediaSourceFactoryWithCache(): DefaultMediaSourceFactory {
//        val client = OkHttpClient()
//            .newBuilder()
//            //.addInterceptor(MediaInterceptor())
//            .build()
//        val networkFactory = OkHttpDataSource.Factory(client)
//        val factory = DefaultDataSource.Factory(this, networkFactory)
//        val dataSourceFactory = CacheDataSource.Factory()
//            .setCache(getCacheDataSource())
//            .setUpstreamDataSourceFactory(factory)
//            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
//        val extractorFactory = ExtractorsFactory {
//            arrayOf(Mp3Extractor())
//        }
//        return DefaultMediaSourceFactory(dataSourceFactory, extractorFactory)
//    }
//
//    private fun getCacheDataSource(): SimpleCache {
//        val cacheDirectory = File(this.cacheDir.absolutePath + "/media")
//        if (!cacheDirectory.exists()) {
//            cacheDirectory.mkdirs()
//        }
//        val evict = LeastRecentlyUsedCacheEvictor(config.cacheMaxSizeBytes)
//        val dbProvider = StandaloneDatabaseProvider(this)
//        cache = SimpleCache(cacheDirectory, evict, dbProvider)
//        return cache
//    }
//
//    /**
//     * ============================================
//     * FIXED: MediaInterceptor - Only intercepts when needed
//     * ============================================
//     */
//    inner class MediaInterceptor : Interceptor {
//        override fun intercept(chain: Interceptor.Chain): Response {
//            val request = chain.request()
//
//            // IMPORTANT: Only intercept if urlFetchCallback is set AND enabled
//            // If callback is null, it means parent project already has direct URLs
//            val fetchCallback = urlFetchCallback?.invoke()
//
//            // If no callback or callback is null, proceed with original request (no interception)
//            if (fetchCallback == null) {
//                // Direct playback - no delay, no API call
//                return chain.proceed(request.newBuilder().build())
//            }
//
//            // Only if callback exists, then fetch fresh URL
//            return try {
//                val track = runBlocking(Dispatchers.Main) {
//                    player.currentMediaItem?.getGenericTrack()
//                } ?: return chain.proceed(request.newBuilder().build())
//
//                val (statusCode, newUrl, errorMessage) = runBlocking(Dispatchers.IO) {
//                    fetchCallback(track)
//                }
//
//                if (statusCode == 200 && !newUrl.isNullOrEmpty()) {
//                    val newRequest = request.newBuilder()
//                        .url(newUrl)
//                        .header("User-Agent", config.userAgent)
//                        .method("GET", null)
//                        .build()
//                    chain.proceed(newRequest)
//                } else {
//                    chain.errorResponse(
//                        request = request,
//                        code = statusCode ?: 500,
//                        message = errorMessage ?: "Invalid or unreachable URL"
//                    )
//                }
//            } catch (e: Exception) {
//                // If any error in callback, proceed with original URL
//                chain.proceed(request.newBuilder().build())
//            }
//        }
//
//        private fun Interceptor.Chain.errorResponse(request: Request, code: Int, message: String): Response {
//            return Response.Builder()
//                .code(code)
//                .protocol(Protocol.HTTP_1_1)
//                .request(request)
//                .message(message)
//                .body(message.toResponseBody(null))
//                .build()
//        }
//    }
//
//    fun setConfiguration(config: GenericMediaConfig) {
//        this.config = config
//    }
//
//    inner class MediaBinder : Binder() {
//        val service: GenericMediaService
//            get() = this@GenericMediaService
//    }
//
//    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
//        mediaSession
//
//    fun stopInitialNotification(removeNotification: Boolean) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            val flag = if (removeNotification) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH
//            stopForeground(flag)
//        } else {
//            @Suppress("DEPRECATION")
//            stopForeground(removeNotification)
//        }
//    }
//
//    fun setSleepTimer(duration: Long) {
//        countDownTimer?.cancel()
//        sleepDurationStart = duration
//        countDownTimer = object : CountDownTimer(duration, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                sleepTimerTickCallback?.invoke(sleepDurationStart, millisUntilFinished, true)
//            }
//            override fun onFinish() {
//                player.pause()
//                removeSleepTimer()
//            }
//        }.start()
//    }
//
//    fun removeSleepTimer() {
//        countDownTimer?.cancel()
//        countDownTimer = null
//        sleepTimerTickCallback?.invoke(0, 0, false)
//    }
//
//    fun stopPlaybackAndService(removeNotification: Boolean) {
//        if (::player.isInitialized) {
//            player.stop()
//            player.release()
//        }
//        if (::cache.isInitialized) cache.release()
//        if (::mediaSession.isInitialized) mediaSession.release()
//        stopInitialNotification(removeNotification)
//        stopSelf()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        removeSleepTimer()
//        stopPlaybackAndService(true)
//    }
//
//    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//        stopPlaybackAndService(true)
//    }
//}

//package com.example.androidwidgetapp.media3Player
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.content.pm.ServiceInfo
//import android.os.Binder
//import android.os.Build
//import android.os.CountDownTimer
//import android.os.IBinder
//import androidx.core.app.NotificationCompat
//import androidx.media3.common.AudioAttributes
//import androidx.media3.common.C
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.database.StandaloneDatabaseProvider
//import androidx.media3.datasource.DefaultDataSource
//import androidx.media3.datasource.cache.CacheDataSource
//import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
//import androidx.media3.datasource.cache.SimpleCache
//import androidx.media3.datasource.okhttp.OkHttpDataSource
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
//import androidx.media3.extractor.ExtractorsFactory
//import androidx.media3.extractor.mp3.Mp3Extractor
//import androidx.media3.session.DefaultMediaNotificationProvider
//import androidx.media3.session.MediaController
//import androidx.media3.session.MediaLibraryService
//import androidx.media3.session.MediaSession
//import androidx.media3.session.SessionToken
//import com.google.common.util.concurrent.MoreExecutors
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import okhttp3.Protocol
//import okhttp3.Request
//import okhttp3.Response
//import okhttp3.ResponseBody.Companion.toResponseBody
//import java.io.File
//import android.content.ComponentName
//import androidx.core.net.toUri
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.runBlocking
//
///**
// * Generic Media3 Service that can be configured for any app
// *
// * Usage:
// * 1. Create a configuration object implementing GenericMediaConfig
// * 2. Pass the config when binding to the service
// * 3. Use GenericMediaServiceHandler to control playback
// */
//@UnstableApi
//class GenericMediaService : MediaLibraryService() {
//
//    lateinit var cache: SimpleCache private set
//    lateinit var player: ExoPlayer private set
//    private var countDownTimer: CountDownTimer? = null
//    private var sleepDurationStart: Long = -1
//
//    // Callbacks
//    var sleepTimerTickCallback: ((durationStart: Long, durationLeft: Long, isActive: Boolean) -> Unit)? = null
//    var urlFetchCallback: (() -> (suspend (GenericTrack) -> Triple<Int?, String?, String?>)?)? = null
//
//    // Flag to control interceptor behavior
//    private var useInterceptor: Boolean = false
//
//    lateinit var mediaSession: MediaLibrarySession private set
//    lateinit var mediaSessionCallback: GenericMediaSessionCallback private set
//    lateinit var notificationProvider: DefaultMediaNotificationProvider private set
//
//    // Configuration
//    private var config: GenericMediaConfig = DefaultMediaConfig()
//
//    companion object {
//        const val DEFAULT_NOTIFICATION_CHANNEL_ID = "generic_media_notification"
//        const val DEFAULT_NOTIFICATION_ID = 1001
//        const val DEFAULT_NOTIFICATION_NAME = "Media"
//        const val DEFAULT_NOTIFICATION_DESCRIPTION = "Media Playback"
//    }
//
//    private val binder = MediaBinder()
//
//    override fun onBind(intent: Intent?): IBinder =
//        super.onBind(intent) ?: binder
//
//    override fun onCreate() {
//        super.onCreate()
//
//        createNotificationChannel(this)
//
//        player = ExoPlayer.Builder(this)
//            .setAudioAttributes(provideAudioAttributes(), true)
//            .setWakeMode(C.WAKE_MODE_NETWORK)
//            .setHandleAudioBecomingNoisy(true)
//            .setSeekForwardIncrementMs(config.seekForwardMs)
//            .setSeekBackIncrementMs(config.seekBackwardMs)
//            .setMediaSourceFactory(getMediaSourceFactoryWithCache())
//            .build()
//
//        mediaSessionCallback = GenericMediaSessionCallback()
//        mediaSession = provideMediaLibrarySession(
//            this,
//            this,
//            player,
//            mediaSessionCallback
//        )
//
//        try {
//            val sessionToken = SessionToken(this, ComponentName(this, GenericMediaService::class.java))
//            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
//            controllerFuture.addListener({ controllerFuture.get() }, MoreExecutors.directExecutor())
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        notificationProvider = DefaultMediaNotificationProvider.Builder(this)
//            .setNotificationId(config.notificationId)
//            .setChannelId(config.notificationChannelId)
//            .setChannelName(config.notificationChannelNameRes)
//            .build().apply {
//                config.notificationIconRes?.let { setSmallIcon(it) }
//            }
//
//        setMediaNotificationProvider(notificationProvider)
//    }
//
//    private fun createNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                config.notificationChannelId,
//                config.notificationChannelName,
//                NotificationManager.IMPORTANCE_LOW
//            )
//            channel.description = config.notificationChannelDescription
//            val manager = context.getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//    }
//
//    fun showInitialNotification() {
//        val notification = buildPlaceholderNotification()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            startForeground(config.notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
//        } else {
//            startForeground(config.notificationId, notification)
//        }
//    }
//
//    private fun buildPlaceholderNotification(): Notification {
//        return NotificationCompat.Builder(this, config.notificationChannelId)
//            .setContentTitle(config.appName)
//            .setContentText("Preparing to play...")
//            .setSmallIcon(config.notificationIconRes ?: android.R.drawable.ic_media_play)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setOnlyAlertOnce(true)
//            .build()
//    }
//
//    private fun provideAudioAttributes(): AudioAttributes =
//        AudioAttributes.Builder()
//            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
//            .setUsage(C.USAGE_MEDIA)
//            .build()
//
//    private fun provideMediaLibrarySession(
//        context: Context,
//        service: MediaLibraryService,
//        player: ExoPlayer,
//        callback: GenericMediaSessionCallback,
//    ): MediaLibrarySession {
//        val track = player.currentMediaItem?.getGenericTrack()
//        val deepLinkUri = config.deepLinkProvider?.invoke(track)?.toUri()
//
//        val pendingIntent = deepLinkUri?.let { uri ->
//            val sessionIntent = Intent(Intent.ACTION_VIEW, uri).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                `package` = context.packageName
//            }
//            PendingIntent.getActivity(
//                context,
//                0,
//                sessionIntent,
//                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            )
//        }
//
//        val builder = MediaLibrarySession.Builder(service, player, callback)
//        pendingIntent?.let { builder.setSessionActivity(it) }
//        config.bitmapLoader?.let { builder.setBitmapLoader(it) }
//
//        return builder.build()
//    }
//
////    private fun getMediaSourceFactoryWithCache(): DefaultMediaSourceFactory {
////        val clientBuilder = OkHttpClient().newBuilder()
////
////        // Only add interceptor if explicitly needed
////        if (useInterceptor) {
////            clientBuilder.addInterceptor(MediaInterceptor())
////        }
////
////        val client = clientBuilder.build()
////        val networkFactory = OkHttpDataSource.Factory(client)
////        val factory = DefaultDataSource.Factory(this, networkFactory)
////        val dataSourceFactory = CacheDataSource.Factory()
////            .setCache(getCacheDataSource())
////            .setUpstreamDataSourceFactory(factory)
////            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
////        val extractorFactory = ExtractorsFactory {
////            arrayOf(Mp3Extractor())
////        }
////        return DefaultMediaSourceFactory(dataSourceFactory, extractorFactory)
////    }
//
//    private fun getMediaSourceFactoryWithCache(): DefaultMediaSourceFactory {
//        val clientBuilder = OkHttpClient().newBuilder()
//        val client = clientBuilder.build()
//        val networkFactory = OkHttpDataSource.Factory(client)
//        val factory = DefaultDataSource.Factory(this, networkFactory)
//
//        // TEMPORARILY DISABLE CACHE
//        val extractorFactory = ExtractorsFactory {
//            arrayOf(Mp3Extractor())
//        }
//        return DefaultMediaSourceFactory(factory, extractorFactory)
//    }
//
//    private fun getCacheDataSource(): SimpleCache {
//        val cacheDirectory = File(this.cacheDir.absolutePath + "/media")
//        if (!cacheDirectory.exists()) {
//            cacheDirectory.mkdirs()
//        }
//        val evict = LeastRecentlyUsedCacheEvictor(config.cacheMaxSizeBytes)
//        val dbProvider = StandaloneDatabaseProvider(this)
//        cache = SimpleCache(cacheDirectory, evict, dbProvider)
//        return cache
//    }
//
//    /**
//     * ============================================
//     * OPTIMIZED: MediaInterceptor - Only used when explicitly enabled
//     * ============================================
//     * This interceptor is NOW OPTIONAL and only activates when:
//     * 1. useInterceptor flag is true
//     * 2. urlFetchCallback is set
//     *
//     * For direct URLs (parent app provides MP3 URLs), this is SKIPPED entirely
//     */
//    inner class MediaInterceptor : Interceptor {
//        override fun intercept(chain: Interceptor.Chain): Response {
//            val request = chain.request()
//
//            // Get callback
//            val fetchCallback = urlFetchCallback?.invoke()
//
//            // If no callback, proceed directly (no processing)
//            if (fetchCallback == null) {
//                return chain.proceed(request.newBuilder().build())
//            }
//
//            // Only if callback exists, then fetch fresh URL
//            return try {
//                val track = runBlocking(Dispatchers.Main) {
//                    player.currentMediaItem?.getGenericTrack()
//                } ?: return chain.proceed(request.newBuilder().build())
//
//                val (statusCode, newUrl, errorMessage) = runBlocking(Dispatchers.IO) {
//                    fetchCallback(track)
//                }
//
//                if (statusCode == 200 && !newUrl.isNullOrEmpty()) {
//                    val newRequest = request.newBuilder()
//                        .url(newUrl)
//                        .header("User-Agent", config.userAgent)
//                        .method("GET", null)
//                        .build()
//                    chain.proceed(newRequest)
//                } else {
//                    chain.errorResponse(
//                        request = request,
//                        code = statusCode ?: 500,
//                        message = errorMessage ?: "Invalid or unreachable URL"
//                    )
//                }
//            } catch (e: Exception) {
//                // If any error in callback, proceed with original URL
//                chain.proceed(request.newBuilder().build())
//            }
//        }
//
//        private fun Interceptor.Chain.errorResponse(request: Request, code: Int, message: String): Response {
//            return Response.Builder()
//                .code(code)
//                .protocol(Protocol.HTTP_1_1)
//                .request(request)
//                .message(message)
//                .body(message.toResponseBody(null))
//                .build()
//        }
//    }
//
//    fun setConfiguration(config: GenericMediaConfig) {
//        this.config = config
//    }
//
//    /**
//     * Enable or disable URL interceptor
//     * Call this from ViewModel when setting URL resolver
//     */
//    fun setInterceptorEnabled(enabled: Boolean) {
//        useInterceptor = enabled
//    }
//
//    inner class MediaBinder : Binder() {
//        val service: GenericMediaService
//            get() = this@GenericMediaService
//    }
//
//    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
//        mediaSession
//
//    fun stopInitialNotification(removeNotification: Boolean) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            val flag = if (removeNotification) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH
//            stopForeground(flag)
//        } else {
//            @Suppress("DEPRECATION")
//            stopForeground(removeNotification)
//        }
//    }
//
//    fun setSleepTimer(duration: Long) {
//        countDownTimer?.cancel()
//        sleepDurationStart = duration
//        countDownTimer = object : CountDownTimer(duration, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                sleepTimerTickCallback?.invoke(sleepDurationStart, millisUntilFinished, true)
//            }
//            override fun onFinish() {
//                player.pause()
//                removeSleepTimer()
//            }
//        }.start()
//    }
//
//    fun removeSleepTimer() {
//        countDownTimer?.cancel()
//        countDownTimer = null
//        sleepTimerTickCallback?.invoke(0, 0, false)
//    }
//
//    fun stopPlaybackAndService(removeNotification: Boolean) {
//        if (::player.isInitialized) {
//            player.stop()
//            player.release()
//        }
//        if (::cache.isInitialized) cache.release()
//        if (::mediaSession.isInitialized) mediaSession.release()
//        stopInitialNotification(removeNotification)
//        stopSelf()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        removeSleepTimer()
//        stopPlaybackAndService(true)
//    }
//
//    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//        stopPlaybackAndService(true)
//    }
//}

package com.example.androidwidgetapp.media3Player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.mp3.Mp3Extractor
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.File
import android.content.ComponentName
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * OPTIMIZED Generic Media3 Service
 * All performance improvements applied
 */
@UnstableApi
class GenericMediaService : MediaLibraryService() {

    lateinit var cache: SimpleCache private set
    lateinit var player: ExoPlayer private set
    private var countDownTimer: CountDownTimer? = null
    private var sleepDurationStart: Long = -1

    // Callbacks
    var sleepTimerTickCallback: ((durationStart: Long, durationLeft: Long, isActive: Boolean) -> Unit)? = null
    var urlFetchCallback: (() -> (suspend (GenericTrack) -> Triple<Int?, String?, String?>)?)? = null

    // Flag to control interceptor behavior
    private var useInterceptor: Boolean = false

    lateinit var mediaSession: MediaLibrarySession private set
    lateinit var mediaSessionCallback: GenericMediaSessionCallback private set
    lateinit var notificationProvider: DefaultMediaNotificationProvider private set

    // Configuration
    private var config: GenericMediaConfig = DefaultMediaConfig()

    companion object {
        const val DEFAULT_NOTIFICATION_CHANNEL_ID = "generic_media_notification"
        const val DEFAULT_NOTIFICATION_ID = 1001
        const val DEFAULT_NOTIFICATION_NAME = "Media"
        const val DEFAULT_NOTIFICATION_DESCRIPTION = "Media Playback"
        private const val TAG = "GenericMediaService"
    }

    private val binder = MediaBinder()

    override fun onBind(intent: Intent?): IBinder {
        return super.onBind(intent) ?: binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== Service onCreate START ===")
        val startTime = System.currentTimeMillis()

        createNotificationChannel(this)
        Log.d(TAG, "Notification channel created: ${System.currentTimeMillis() - startTime}ms")

        // Initialize player with optimized settings
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(provideAudioAttributes(), true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .setSeekForwardIncrementMs(config.seekForwardMs)
            .setSeekBackIncrementMs(config.seekBackwardMs)
            .setMediaSourceFactory(getMediaSourceFactoryWithCache())
            .build()

        Log.d(TAG, "ExoPlayer created: ${System.currentTimeMillis() - startTime}ms")

        mediaSessionCallback = GenericMediaSessionCallback()
        mediaSession = provideMediaLibrarySession(
            this,
            this,
            player,
            mediaSessionCallback
        )

        Log.d(TAG, "MediaSession created: ${System.currentTimeMillis() - startTime}ms")

        try {
            val sessionToken = SessionToken(this, ComponentName(this, GenericMediaService::class.java))
            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
            controllerFuture.addListener({ controllerFuture.get() }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setNotificationId(config.notificationId)
            .setChannelId(config.notificationChannelId)
            .setChannelName(config.notificationChannelNameRes)
            .build().apply {
                config.notificationIconRes?.let { setSmallIcon(it) }
            }

        setMediaNotificationProvider(notificationProvider)

        Log.d(TAG, "=== Service onCreate END: ${System.currentTimeMillis() - startTime}ms ===")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                config.notificationChannelId,
                config.notificationChannelName,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = config.notificationChannelDescription
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showInitialNotification() {
        val notification = buildPlaceholderNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(config.notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(config.notificationId, notification)
        }
    }

    private fun buildPlaceholderNotification(): Notification {
        return NotificationCompat.Builder(this, config.notificationChannelId)
            .setContentTitle(config.appName)
            .setContentText("Preparing to play...")
            .setSmallIcon(config.notificationIconRes ?: android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun provideAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    private fun provideMediaLibrarySession(
        context: Context,
        service: MediaLibraryService,
        player: ExoPlayer,
        callback: GenericMediaSessionCallback,
    ): MediaLibrarySession {
        val track = player.currentMediaItem?.getGenericTrack()
        val deepLinkUri = config.deepLinkProvider?.invoke(track)?.toUri()

        val pendingIntent = deepLinkUri?.let { uri ->
            val sessionIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                `package` = context.packageName
            }
            PendingIntent.getActivity(
                context,
                0,
                sessionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val builder = MediaLibrarySession.Builder(service, player, callback)
        pendingIntent?.let { builder.setSessionActivity(it) }
        config.bitmapLoader?.let { builder.setBitmapLoader(it) }

        return builder.build()
    }

    /**
     * OPTIMIZED: Media source factory with optional cache and no interceptor by default
     */
    private fun getMediaSourceFactoryWithCache(): DefaultMediaSourceFactory {
        Log.d(TAG, "Building media source factory, useInterceptor=$useInterceptor")

        // Build OkHttp client with optimizations
        val clientBuilder = OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        // ONLY add interceptor if explicitly enabled
        if (useInterceptor) {
            Log.d(TAG, "Adding MediaInterceptor")
            clientBuilder.addInterceptor(MediaInterceptor())
        } else {
            Log.d(TAG, "Skipping MediaInterceptor for better performance")
        }

        val client = clientBuilder.build()
        val networkFactory = OkHttpDataSource.Factory(client)
        val factory = DefaultDataSource.Factory(this, networkFactory)

        // Use singleton cache for better performance
        val dataSourceFactory = CacheDataSource.Factory()
            .setCache(getCacheDataSource())
            .setUpstreamDataSourceFactory(factory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val extractorFactory = ExtractorsFactory {
            arrayOf(Mp3Extractor())
        }

        return DefaultMediaSourceFactory(dataSourceFactory, extractorFactory)
    }

    /**
     * OPTIMIZED: Use singleton cache instance
     */
    private fun getCacheDataSource(): SimpleCache {
        cache = CacheHolder.getCache(this, config.cacheMaxSizeBytes)
        return cache
    }

    /**
     * OPTIONAL: MediaInterceptor - only used when explicitly enabled
     */
    inner class MediaInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            // Get callback
            val fetchCallback = urlFetchCallback?.invoke()

            // If no callback, proceed directly
            if (fetchCallback == null) {
                return chain.proceed(request.newBuilder().build())
            }

            // Fetch fresh URL
            return try {
                val track = runBlocking(Dispatchers.Main) {
                    player.currentMediaItem?.getGenericTrack()
                } ?: return chain.proceed(request.newBuilder().build())

                val (statusCode, newUrl, errorMessage) = runBlocking(Dispatchers.IO) {
                    fetchCallback(track)
                }

                if (statusCode == 200 && !newUrl.isNullOrEmpty()) {
                    val newRequest = request.newBuilder()
                        .url(newUrl)
                        .header("User-Agent", config.userAgent)
                        .method("GET", null)
                        .build()
                    chain.proceed(newRequest)
                } else {
                    chain.errorResponse(
                        request = request,
                        code = statusCode ?: 500,
                        message = errorMessage ?: "Invalid or unreachable URL"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Interceptor error", e)
                chain.proceed(request.newBuilder().build())
            }
        }

        private fun Interceptor.Chain.errorResponse(request: Request, code: Int, message: String): Response {
            return Response.Builder()
                .code(code)
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .message(message)
                .body(message.toResponseBody(null))
                .build()
        }
    }

    fun setConfiguration(config: GenericMediaConfig) {
        this.config = config
    }

    fun setInterceptorEnabled(enabled: Boolean) {
        useInterceptor = enabled
        Log.d(TAG, "Interceptor enabled: $enabled")
    }

    inner class MediaBinder : Binder() {
        val service: GenericMediaService
            get() = this@GenericMediaService
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaSession

    fun stopInitialNotification(removeNotification: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val flag = if (removeNotification) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH
            stopForeground(flag)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(removeNotification)
        }
    }

    fun setSleepTimer(duration: Long) {
        countDownTimer?.cancel()
        sleepDurationStart = duration
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                sleepTimerTickCallback?.invoke(sleepDurationStart, millisUntilFinished, true)
            }
            override fun onFinish() {
                player.pause()
                removeSleepTimer()
            }
        }.start()
    }

    fun removeSleepTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        sleepTimerTickCallback?.invoke(0, 0, false)
    }

    fun stopPlaybackAndService(removeNotification: Boolean) {
        if (::player.isInitialized) {
            player.stop()
            player.release()
        }
        if (::mediaSession.isInitialized) mediaSession.release()
        stopInitialNotification(removeNotification)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeSleepTimer()
        stopPlaybackAndService(true)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopPlaybackAndService(true)
    }
}

/**
 * OPTIMIZATION: Singleton cache holder
 * Cache is created once and reused across service restarts
 */
@UnstableApi
private object CacheHolder {
    @Volatile
    private var INSTANCE: SimpleCache? = null

    fun getCache(context: Context, maxSize: Long): SimpleCache {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: run {
                Log.d("CacheHolder", "Creating new cache instance")
                val cacheDirectory = File(context.cacheDir.absolutePath + "/media")
                if (!cacheDirectory.exists()) {
                    cacheDirectory.mkdirs()
                }
                val evict = LeastRecentlyUsedCacheEvictor(maxSize)
                val dbProvider = StandaloneDatabaseProvider(context)
                SimpleCache(cacheDirectory, evict, dbProvider).also {
                    INSTANCE = it
                    Log.d("CacheHolder", "Cache created successfully")
                }
            }
        }
    }

    fun releaseCache() {
        synchronized(this) {
            INSTANCE?.release()
            INSTANCE = null
        }
    }
}