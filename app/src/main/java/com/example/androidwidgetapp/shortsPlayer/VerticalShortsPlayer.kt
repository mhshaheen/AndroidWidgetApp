package com.example.androidwidgetapp.shortsPlayer

//noinspection SuspiciousImport
import android.R
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
//import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.androidwidgetapp.shortsPlayer.extension.LocalFragmentVisibility
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun VerticalShortsPlayer(
    contents: List<Content>, isMuted: Boolean = true
) {
    val pagerState = rememberPagerState(pageCount = { contents.size })
    val context = LocalContext.current

    VerticalPager(
        modifier = Modifier.fillMaxSize(), state = pagerState
    ) { page: Int ->
        // <-- This lambda is inside PagerScope, matching ComposableFunction2<PagerScope, Int, Unit>
        val content = contents[page]
        val uri = remember(content.streamingUrl) {
            Uri.parse(content.streamingUrl)
        }

        val player = remember(uri) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                repeatMode = Player.REPEAT_MODE_ONE
                volume = if (isMuted) 0f else 1f
            }
        }

        LaunchedEffect(pagerState.settledPage) {
            player.playWhenReady = (pagerState.settledPage == page)
        }

        DisposableEffect(player) {
            onDispose { player.release() }
        }

        PlayerMainScreen(player)
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerMainScreen(
    player: Player,
    hasTapEnable: Boolean = true,
    onDoubleTap: ((Offset) -> Unit)? = null
) {
    var showPlayIcon by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                }
            },
            update = { playerView -> playerView.player = player },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(hasTapEnable) {
                    if (hasTapEnable) {
                        detectTapGestures(
                            onTap = {
                                val wasPlaying = player.isPlaying
                                player.playWhenReady = !wasPlaying
                                showPlayIcon = wasPlaying
                            },
                            onDoubleTap = onDoubleTap
                        )
                    }
                }
        )

        AnimatedVisibility(
            visible = showPlayIcon,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        player.playWhenReady = true
                        showPlayIcon = false
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_media_play),
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}



@OptIn(UnstableApi::class)
@Composable
fun rememberShortsPlayer(
    content: Content,
    isVisiblePage: Boolean,
    isMuted: Boolean = false,
    //onAnalyticsReady: (PlayerAnalytics) -> Unit = {},
    onIsPlayingChanged: (Boolean) -> Unit = {},
    onPlayerProcess: (currentProgress: Float) -> Unit = {},
    onLoadingStateChanged: (Boolean) -> Unit = {},
): ExoPlayer {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val isFragmentVisible = LocalFragmentVisibility.current

//    val mediaSourceFactory = remember {
//        ShortsCache.getMediaSourceFactory(
//            context,
//            PlayerInterceptor()
//        )
//    }

    val player = remember(content.streamingUrl) {
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setForceLowestBitrate(true)
                    .setAllowVideoMixedMimeTypeAdaptiveness(true)
            )
        }

        val loadControl = DefaultLoadControl.Builder()
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBufferDurationsMs(
                /* minBufferMs                      */ 1_000,
                /* maxBufferMs                      */ if (isVisiblePage) 8_000 else 4_000,
                /* bufferForPlaybackMs              */   400,
                /* bufferForPlaybackAfterRebufferMs */ 400,
            )
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context)
            .setRenderersFactory(DefaultRenderersFactory(context).setEnableDecoderFallback(true))
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setBandwidthMeter(bandwidthMeter)
            .setAudioAttributes(audioAttributes, true)
            //.setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            }
    }

//    LaunchedEffect(content.streamingUrl) {
//        val item = MediaItem.fromUri(content.streamingUrl)
//        val mediaSource = mediaSourceFactory.createMediaSource(item)
//        player.setMediaSource(mediaSource)
//        player.prepare()


//        player.setMediaSource(
//            mediaSourceFactory
//                .createMediaSource(content.toMediaItem())
//                .toClippingMediaSource(content)
//        )
//        player.prepare()
//    }

    LaunchedEffect(Unit) {
        context.networkStatusFlow()
            .distinctUntilChanged()
            .collect { connected ->
                if (connected) player.prepare()
            }
    }

    LaunchedEffect(player, isVisiblePage, isMuted) {
        runCatching {
            player.volume = if (isMuted) 0f else 1f
            if (isVisiblePage && isFragmentVisible()) {
                player.playWhenReady = true
//                ShortsMainFragment.onBackToPlayer = {
//                    if (isFragmentVisible()){
//                        player.playWhenReady = true
//                    }
//                }
//                ShortsMainFragment.onFragmentPaused = {
//                    if (!isFragmentVisible()) player.pause()
//                }
            }else{
                player.playWhenReady = false
            }
        }.onFailure {
            Log.e("ShortsPlayer", "Player error: ${it.message}")
        }
    }

    DisposableEffect(player, lifecycleOwner) {
        val lifeObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP  -> player.playWhenReady = false
                Lifecycle.Event.ON_START -> {
                    if (isVisiblePage && isFragmentVisible()) player.playWhenReady = true
                }
                else                     -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifeObserver)

        val progressJob = scope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    onPlayerProcess(player.currentPosition.toFloat())
                }
                delay(250)
            }
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifeObserver)

            progressJob.cancel()
            player.playWhenReady = false
            player.stop()
            player.release()
        }
    }

    return player
}

fun Context.networkStatusFlow(): Flow<Boolean> = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Emit current connectivity state initially
    fun sendCurrentConnectivityStatus() {
        val isConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork != null &&
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
        trySend(isConnected)
    }

    sendCurrentConnectivityStatus()

    // Create and register network callback
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(true)
        }

        override fun onLost(network: Network) {
            trySend(false)
        }
    }

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    try {
        connectivityManager.registerNetworkCallback(request, callback)
    } catch (e: SecurityException) {
        close(e) // In case ACCESS_NETWORK_STATE is missing
    } catch (e: Exception) {
        close(e)
    }

    awaitClose {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (_: Exception) {
            // Ignore unregister errors
        }
    }
}