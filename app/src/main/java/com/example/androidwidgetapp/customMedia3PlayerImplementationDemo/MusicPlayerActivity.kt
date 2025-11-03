package com.example.androidwidgetapp.customMedia3PlayerImplementationDemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidwidgetapp.media3Player.GenericTrack.Companion.safeJson
import com.sm_fs.custommedia3player.domain.model.PlaybackProgress
import com.sm_fs.custommedia3player.domain.model.PlayerState
import com.sm_fs.custommedia3player.domain.model.Track
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.String


val trackList = listOf(
    MyClass(
        contentId = "1",
        contentType = "A",
        title = "Beautiful Sunrise",
        artist = "Morning Band",
        artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
        playbackUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        durationMs = 180000,
        albumName = "Dawn Collection",
        genre = "Ambient",
    ),
    MyClass(
        contentId = "2",
        contentType = "A",
        title = "Evening Blues",
        artist = "Night Orchestra",
        artworkUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600",
        playbackUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        durationMs = 180000,
        albumName = "Sunset Vibes",
        genre = "Jazz",
    ),
    MyClass(
        contentId = "3",
        contentType = "A",
        title = "Mountain Echo",
        artist = "Valley Singers",
        artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
        playbackUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        durationMs = 180000,
        albumName = "Nature Sounds",
        genre = "Folk",
    ),
    MyClass(
        contentId = "4",
        contentType = "A",
        title = "City Lights",
        artist = "Urban Beat",
        artworkUrl = "https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?w=600",
        playbackUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
        durationMs = 180000,
        albumName = "Downtown",
        genre = "Electronic",
    ),
    MyClass(
        contentId = "5",
        contentType = "A",
        title = "Ocean Waves",
        artist = "Coastal Crew",
        artworkUrl = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=600",
        playbackUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
        durationMs = 180,
        albumName = "Beach Sessions",
        genre = "Chill",
    ),
)

@Serializable
data class MyClass(
    override val contentId: String,
    override val contentType: String,
    override val title: String = "",
    override val artist: String = "",
    override val artworkUrl: String = "",
    override val playbackUrl: String = "",
    override val durationMs: Long = 0,
    val albumName: String = "",
    val genre: String = "",
) : Track {
    override val metadata: Map<String, String>
        get() = mapOf("MyClass" to safeJson.encodeToString<MyClass>(this))
}

class MusicPlayerActivity : ComponentActivity() {

    private val viewModel: MusicPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val viewModel = MusicPlayerViewModel(application)

        setContent {
            MaterialTheme {
                MusicPlayerScreen(viewModel)
            }
        }
    }
}

@Composable
fun MusicPlayerScreen(viewModel: MusicPlayerViewModel) {

    val safeJson = remember {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    //val myClass = remember { MyClass() }

    val currentTrack by viewModel.currentTrack.observeAsState()
    val playerState  by viewModel.playerState.observeAsState(PlayerState.Idle)

    try {
        Log.d(
            "MusicPlayerScreen",
            "currentTrack MyClass: ${safeJson.decodeFromString<MyClass>(currentTrack?.metadata["MyClass"] ?: "")}"
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Track Info
        currentTrack?.let { track ->
            Text(text = track.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = track.artist ?: "Unknown", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(24.dp))

            val progress by viewModel.progress.observeAsState(PlaybackProgress(0, 0, 0))
            // Progress Bar
            Slider(
                value = progress.currentPositionMs.toFloat(),
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..progress.durationMs.toFloat()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(progress.currentPositionMs))
                Text(formatTime(progress.durationMs))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(Icons.Default.Shuffle, "Shuffle")
                }
                IconButton(onClick = { viewModel.previous() }) {
                    Icon(Icons.Default.SkipPrevious, "Previous")
                }
                FloatingActionButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(
                        if (playerState is PlayerState.Playing)
                            Icons.Default.Pause
                        else
                            Icons.Default.PlayArrow,
                        "Play/Pause"
                    )
                }
                IconButton(onClick = { viewModel.next() }) {
                    Icon(Icons.Default.SkipNext, "Next")
                }
                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    Icon(Icons.Default.Repeat, "Repeat")
                }
            }
        } ?: run {
            // No track playing
            Button(onClick = {
                viewModel.playTracks(trackList)
            }) {
                Text("Play Demo")
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 1000 / 60) % 60
    return "%02d:%02d".format(minutes, seconds)
}