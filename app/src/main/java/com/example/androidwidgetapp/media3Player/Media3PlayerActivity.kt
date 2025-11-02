package com.example.androidwidgetapp.media3Player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidwidgetapp.R
import kotlinx.serialization.Serializable

/**
 * ============================================
 * PARENT PROJECT DATA MODEL (Demo Purpose)
 * ============================================
 * This represents your parent project's track structure
 */
data class ParentProjectTrack(
    val trackId: String,
    val trackTitle: String,
    val artistName: String?,
    val albumArt: String?,
    val streamUrl: String,
    val durationSeconds: Long,
    val albumName: String? = null,
    val genre: String? = null
)

/**
 * ============================================
 * DEMO DATA SOURCE (Simulating API Response)
 * ============================================
 * In real app, this would come from your API
 */
object DemoMusicData {

    fun getTracksFromParentProject(): List<ParentProjectTrack> {
        return listOf(
            ParentProjectTrack(
                trackId = "track_001",
                trackTitle = "Beautiful Sunrise",
                artistName = "Morning Band",
                albumArt = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                durationSeconds = 245,
                albumName = "Dawn Collection",
                genre = "Ambient"
            ),
            ParentProjectTrack(
                trackId = "track_002",
                trackTitle = "Evening Blues",
                artistName = "Night Orchestra",
                albumArt = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                durationSeconds = 312,
                albumName = "Sunset Vibes",
                genre = "Jazz"
            ),
            ParentProjectTrack(
                trackId = "track_003",
                trackTitle = "Mountain Echo",
                artistName = "Valley Singers",
                albumArt = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                durationSeconds = 198,
                albumName = "Nature Sounds",
                genre = "Folk"
            ),
            ParentProjectTrack(
                trackId = "track_004",
                trackTitle = "City Lights",
                artistName = "Urban Beat",
                albumArt = "https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?w=600",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                durationSeconds = 267,
                albumName = "Downtown",
                genre = "Electronic"
            ),
            ParentProjectTrack(
                trackId = "track_005",
                trackTitle = "Ocean Waves",
                artistName = "Coastal Crew",
                albumArt = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=600",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                durationSeconds = 289,
                albumName = "Beach Sessions",
                genre = "Chill"
            )
        )
    }
}

/**
 * ============================================
 * YOUR TRACK MODEL (Extends GenericTrack)
 * ============================================
 */
@Serializable
data class MyTrack(
    override val id: String,
    override val title: String,
    override val artist: String?,
    override val artworkUrl: String?,
    override val playUrl: String,
    override val duration: Long = 0L,
    override val currentDurationCursor: Long = 0L,
    override var isCurrentlyPlaying: Boolean = false,
    val albumName: String? = null,
    val genre: String? = null
) : GenericTrack {
    override fun copy(): GenericTrack = MyTrack(
        id = this.id,
        title = this.title,
        artist = this.artist,
        artworkUrl = this.artworkUrl,
        playUrl = this.playUrl,
        duration = this.duration,
        currentDurationCursor = this.currentDurationCursor,
        isCurrentlyPlaying = this.isCurrentlyPlaying,
        albumName = this.albumName,
        genre = this.genre
    )
}

/**
 * ============================================
 * CONVERTER: Parent Project -> Generic Track
 * ============================================
 */
fun ParentProjectTrack.toMyTrack(): MyTrack {
    return MyTrack(
        id = this.trackId,
        title = this.trackTitle,
        artist = this.artistName,
        artworkUrl = this.albumArt,
        playUrl = this.streamUrl,
        duration = this.durationSeconds,
        currentDurationCursor = 0L,
        isCurrentlyPlaying = false,
        albumName = this.albumName,
        genre = this.genre
    )
}

/**
 * ============================================
 * PLAYER CONFIGURATION
 * ============================================
 */
//fun createMyAppConfig(context: Context): GenericMediaConfig {
//    return DefaultMediaConfig(
//        appName = "My Music App",
//        notificationChannelId = "my_music_channel",
//        notificationChannelName = "Music Player",
//        notificationChannelDescription = "Playing music from My App",
//        notificationChannelNameRes = R.string.app_name,
//        notificationId = 2001,
//        notificationIconRes = R.drawable.ic_sub_plan,
//        seekForwardMs = 10000L, // 10 seconds
//        seekBackwardMs = 10000L,
//        cacheMaxSizeBytes = 500 * 1024 * 1024L, // 500 MB
//        userAgent = "MyMusicApp/1.0",
//        deepLinkProvider = { track ->
//            "myapp://player?id=${track?.id}"
//        },
//        bitmapLoader = null
//    )
//}

fun createMyAppConfig(context: Context): GenericMediaConfig {
    return DefaultMediaConfig(
        appName = "My Music App",
        notificationChannelId = "my_music_channel",
        notificationChannelName = "Music Player",
        notificationChannelDescription = "Playing music",
        notificationChannelNameRes = android.R.string.unknownName, // Changed
        notificationId = 2001,
        notificationIconRes = android.R.drawable.ic_media_play, // Changed to default
        seekForwardMs = 5000L, // Changed to default
        seekBackwardMs = 5000L, // Changed to default
        cacheMaxSizeBytes = 100 * 1024 * 1024L, // Reduced
        userAgent = "MyMusicApp/1.0"
        // Removed deepLinkProvider
    )
}

/**
 * ============================================
 * MAIN ACTIVITY
 * ============================================
 */
class Media3PlayerActivity : ComponentActivity() {

    private lateinit var playerViewModel: GenericMediaViewModel
    val startTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("PERF_TEST", "=== Activity onCreate START ===")

        // Initialize ViewModel
        playerViewModel = GenericMediaViewModel(application)
        Log.d("PERF_TEST", "ViewModel created: ${System.currentTimeMillis() - startTime}ms")

        // Configure the player
        playerViewModel.configure(createMyAppConfig(this))
        Log.d("PERF_TEST", "Config set: ${System.currentTimeMillis() - startTime}ms")

        // Set URL resolver (optional - only if you need to fetch streaming URL separately)
//        playerViewModel.setUrlResolver { track ->
//            // For demo, we already have the URL in our data
//            // In real app, you would call your API here to get fresh streaming URL
//            Triple(200, track.playUrl, null)
//        }

        playerViewModel.enableUrlInterceptor(false)
        Log.d("PERF_TEST", "Interceptor disabled: ${System.currentTimeMillis() - startTime}ms")

        // Set analytics callback (optional)
        playerViewModel.setAnalyticsCallback { analytics ->
            // Send analytics to your backend
            sendAnalyticsToServer(analytics)
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyMusicPlayerScreen(playerViewModel)
                }
            }
        }
        Log.d("PERF_TEST", "=== Activity onCreate END: ${System.currentTimeMillis() - startTime}ms ===")
    }

    private fun sendAnalyticsToServer(analytics: GenericAnalytics) {
        // Send to your analytics service
        println("Analytics: ${analytics.track.title} played for ${analytics.playedMS}ms")
    }

    override fun onStop() {
        super.onStop()
        // Don't call playerViewModel.onStop() here if you want music to continue in background
        // Only call it if you want to stop music when activity is destroyed
    }

    override fun onDestroy() {
        super.onDestroy()
        // Uncomment below if you want to stop playback when activity is destroyed
        // playerViewModel.onStop()
    }
}

/**
 * ============================================
 * UI SCREEN
 * ============================================
 */
@Composable
fun MyMusicPlayerScreen(viewModel: GenericMediaViewModel) {
    val nowPlaying by viewModel.nowPlaying.observeAsState()
    val playerState by viewModel.playerState.observeAsState(GenericPlayerState.IDLE)
    val progress by viewModel.progress.observeAsState(Triple(0L, 0L, 0))
    val playlist by viewModel.playlist.observeAsState(emptyList())
    val shuffleEnabled by viewModel.shuffleModeEnabled.observeAsState(false)
    val repeatMode by viewModel.repeatMode.observeAsState(GenericRepeatMode.NONE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        nowPlaying?.let { track ->
            // ========== NOW PLAYING INFO ==========
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = track.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ========== PROGRESS BAR ==========
            val (currentPos, duration, buffered) = progress
            if (duration > 0) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentPos.toFloat(),
                        onValueChange = { newPos ->
                            viewModel.seekTo(newPos.toLong())
                        },
                        valueRange = 0f..duration.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPos),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Buffered: $buffered%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatTime(duration),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ========== PLAYBACK CONTROLS ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                // Previous
                IconButton(onClick = { viewModel.previous() }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play/Pause
                FloatingActionButton(
                    onClick = { viewModel.togglePlayPause() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = when (playerState) {
                            GenericPlayerState.PLAYING -> Icons.Default.Pause
                            GenericPlayerState.BUFFERING -> Icons.Default.HourglassEmpty
                            else -> Icons.Default.PlayArrow
                        },
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Next
                IconButton(onClick = { viewModel.next() }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat
                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    Icon(
                        imageVector = when (repeatMode) {
                            GenericRepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != GenericRepeatMode.NONE)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== PLAYBACK SPEED ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Playback Speed",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            Button(
                                onClick = { viewModel.setPlaybackSpeed(speed) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(
                                    text = "${speed}x",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== PLAYLIST INFO ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Playlist",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${playlist.size} tracks • Now playing: #${viewModel.nowPlayingPosition() + 1}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

        } ?: run {
            // ========== NO TRACK PLAYING ==========
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "No track playing",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Load tracks from parent project",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        Log.d("PERF_TEST", "=== PLAY BUTTON CLICKED ===")
                        val startTime = System.currentTimeMillis()
                        // ============================================
                        // THIS IS THE KEY INTEGRATION POINT
                        // ============================================

                        // STEP 1: Get data from parent project (simulating API call)
                        val parentProjectTracks = DemoMusicData.getTracksFromParentProject()
                        Log.d("PERF_TEST", "Got demo data: ${System.currentTimeMillis() - startTime}ms")

                        // STEP 2: Convert to your track model (which implements GenericTrack)
                        val myTracks: List<MyTrack> = parentProjectTracks.map { it.toMyTrack() }
                        Log.d("PERF_TEST", "Converted tracks: ${System.currentTimeMillis() - startTime}ms")

                        // STEP 3: Pass to Generic Media3 Player and start playing
                        viewModel.startPlaying(
                            tracks = myTracks,
                            positionToPlay = 0,
                            isShuffled = false
                        )
                        Log.d("PERF_TEST", "Called startPlaying: ${System.currentTimeMillis() - startTime}ms")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Load & Play Demo Tracks")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Demo Info",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• 5 demo tracks will be loaded\n• Data comes from ParentProjectTrack model\n• Converted to MyTrack (GenericTrack)\n• Passed to Generic Media3 Player",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}