package com.sm_fs.custommedia3player.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.sm_fs.custommedia3player.R
import com.sm_fs.custommedia3player.core.analytics.AnalyticsCollector
import com.sm_fs.custommedia3player.core.config.NotificationConfiguration
import com.sm_fs.custommedia3player.core.session.MediaSessionCallback
import com.sm_fs.custommedia3player.data.interceptor.UrlInterceptor
import com.sm_fs.custommedia3player.data.source.MediaSourceFactoryProvider
import com.sm_fs.custommedia3player.domain.callback.PlayerConfiguration
import com.sm_fs.custommedia3player.domain.model.Track
import com.sm_fs.custommedia3player.util.extensions.CustomBitmapLoader
import com.sm_fs.custommedia3player.util.extensions.toTrack


@UnstableApi
class GenericMediaService : MediaLibraryService() {

    companion object {
        private const val TAG = "GenericMediaService"
    }

    lateinit var player: ExoPlayer private set
    lateinit var mediaSession: MediaLibraryService.MediaLibrarySession private set
    lateinit var notificationProvider: DefaultMediaNotificationProvider private set
    private var playerConfig: PlayerConfiguration = PlayerConfiguration.Builder().build()
    private var notificationConfig: NotificationConfiguration = NotificationConfiguration()

    private var bitmapLoader: CustomBitmapLoader? = null

    private var sleepTimer: CountDownTimer? = null

    var getCurrentTrack: (() -> Track?)? = null
    var onSleepTimerTick: ((remainingMs: Long) -> Unit)? = null

    inner class ServiceBinder : Binder() {
        fun getService(): GenericMediaService = this@GenericMediaService
    }

    private val binder = ServiceBinder()

    override fun onBind(intent: Intent?): IBinder? {
        val superBinder = super.onBind(intent)
        Log.d(TAG, "onBind called, superBinder: $superBinder")
        return superBinder ?: binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializePlayer()
        initializeMediaSession()
        setupNotificationProvider()
    }

    fun initialize(
        playerConfig: PlayerConfiguration, notificationConfig: NotificationConfiguration
    ) {
        this.playerConfig = playerConfig
        this.notificationConfig = notificationConfig
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationConfig.channelId,
                notificationConfig.channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = notificationConfig.channelDescription
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun setupNotificationProvider() {
        notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(notificationConfig.channelId)
            .setNotificationId(notificationConfig.notificationId)
            .setChannelName(R.string.channel_name).build().apply {
                notificationConfig.smallIconRes?.let {
                    this.setSmallIcon(it)
                }
            }
        setMediaNotificationProvider(notificationProvider)
    }

    private fun initializePlayer() {
        try {
            val urlInterceptor = playerConfig.urlResolver?.let {
                UrlInterceptor(it) { getCurrentTrack?.invoke() }
            }

            val mediaSourceFactory =
                MediaSourceFactoryProvider(this, playerConfig).create(urlInterceptor)

            player = ExoPlayer.Builder(this).setAudioAttributes(createAudioAttributes(), true)
                .setWakeMode(C.WAKE_MODE_NETWORK).setHandleAudioBecomingNoisy(true)
                .setSeekForwardIncrementMs(playerConfig.seekIncrementMs)
                .setSeekBackIncrementMs(playerConfig.seekIncrementMs)
                .setMediaSourceFactory(mediaSourceFactory).build()

            if (playerConfig.enableAnalytics) {
                val analyticsCollector = AnalyticsCollector(playerConfig.customAnalyticsListener)
                player.addAnalyticsListener(analyticsCollector)
            }

            Log.d(TAG, "✓ Player initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to initialize player", e)
            throw e
        }
    }

    private fun createAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA).build()
    }

    private fun initializeMediaSession() {
        val callback = MediaSessionCallback()
        mediaSession = provideMediaLibrarySession(
            this, this, player, callback
        )
        try {
            val sessionToken =
                SessionToken(this, ComponentName(this, GenericMediaService::class.java))
            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
            controllerFuture.addListener({ controllerFuture.get() }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibraryService.MediaLibrarySession? {
        Log.d(TAG, "onGetSession called for: ${controllerInfo.packageName}")
        return mediaSession
    }

    @UnstableApi
    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, startInForegroundRequired)
    }

    fun showInitialNotification() {
        val notification = buildPlaceholderNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                notificationConfig.notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(notificationConfig.notificationId, notification)
        }
    }

    private fun buildPlaceholderNotification(): Notification {
        return NotificationCompat.Builder(this, notificationConfig.channelId)
            .setContentTitle("Custom Music").setContentText("Preparing to play...").setSmallIcon(
                notificationConfig.smallIconRes
                    ?: androidx.media3.session.R.drawable.media3_notification_small_icon
            ).setPriority(NotificationCompat.PRIORITY_LOW).setOnlyAlertOnce(true).build()
    }

    private fun provideMediaLibrarySession(
        context: Context,
        service: MediaLibraryService,
        player: ExoPlayer,
        callback: MediaSessionCallback,
    ): MediaLibrarySession {
        val smTrack = player.currentMediaItem?.toTrack()
        val deepLinkUri =
            "https://shadhinmusic.com/player?contentId=${smTrack?.contentId}&type=${smTrack?.contentType}".toUri()
        val sessionIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            `package` = context.packageName
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            sessionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        bitmapLoader = CustomBitmapLoader(context)

        return MediaLibrarySession.Builder(service, player, callback)
            .setSessionActivity(pendingIntent)
            .setBitmapLoader(bitmapLoader!!)
            .build()
    }

    fun setSleepTimer(durationMs: Long) {
        sleepTimer?.cancel()
        sleepTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                onSleepTimerTick?.invoke(millisUntilFinished)
            }

            override fun onFinish() {
                player.pause()
                onSleepTimerTick?.invoke(0L)
            }
        }.start()
        Log.d(TAG, "Sleep timer set: ${durationMs}ms")
    }

    fun cancelSleepTimer() {
        sleepTimer?.cancel()
        sleepTimer = null
        onSleepTimerTick?.invoke(0L)
        Log.d(TAG, "Sleep timer cancelled")
    }

    override fun onDestroy() {
        bitmapLoader?.release()
        bitmapLoader = null
        Log.d(TAG, "onDestroy called")
        cancelSleepTimer()

        if (::mediaSession.isInitialized) {
            mediaSession.release()
        }

        if (::player.isInitialized) {
            player.release()
        }

        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved - isPlaying: ${player.isPlaying}")
        if (!player.isPlaying) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }
}