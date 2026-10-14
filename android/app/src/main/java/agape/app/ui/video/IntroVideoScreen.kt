package agape.app.ui.video

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import agape.app.ui.theme.ByzantineGold
import agape.app.ui.theme.ParchmentLight
import agape.app.ui.theme.SacredSerif
import kotlinx.coroutines.delay

object IntroPreferences {
    private const val PREFS_NAME = "agape_onboarding_prefs"
    private const val KEY_HAS_SEEN_INTRO = "has_seen_intro_video"

    fun shouldShowIntro(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return !prefs.getBoolean(KEY_HAS_SEEN_INTRO, false)
    }

    fun markIntroSeen(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_SEEN_INTRO, true)
            .apply()
    }
}

@OptIn(UnstableApi::class)
@Composable
fun IntroVideoScreen(
    maxDurationMillis: Long = 5500L,
    onIntroFinished: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isExiting by remember { mutableStateOf(false) }

    val completeIntro = remember {
        {
            if (!isExiting) {
                isExiting = true
                IntroPreferences.markIntroSeen(context)
                onIntroFinished()
            }
        }
    }

    val firstFilmPath = remember {
        runCatching {
            val films = context.assets.list("films")
                ?.filter { it.endsWith(".mp4", true) || it.endsWith(".webm", true) }
            films?.firstOrNull()?.let { "films/$it" }
        }.getOrNull()
    }

    val exoPlayer = remember(firstFilmPath) {
        ExoPlayer.Builder(context).build().apply {
            if (firstFilmPath != null) {
                val assetUri = Uri.parse("asset:///$firstFilmPath")
                setMediaItem(MediaItem.fromUri(assetUri))
                repeatMode = Player.REPEAT_MODE_OFF
                volume = 0.85f
                prepare()
                playWhenReady = true
            } else {
                completeIntro()
            }
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        completeIntro()
                    }
                }
            })
        }
    }

    LaunchedEffect(Unit) {
        delay(maxDurationMillis)
        completeIntro()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0A08))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.82f)
                        )
                    )
                )
        )

        OutlinedButton(
            onClick = completeIntro,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Black.copy(alpha = 0.45f),
                contentColor = ParchmentLight
            ),
            border = BorderStroke(1.dp, ByzantineGold.copy(alpha = 0.7f))
        ) {
            Text(
                text = "Overslaan",
                style = MaterialTheme.typography.labelLarge
            )
        }

        AnimatedVisibility(
            visible = !isExiting,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "☩",
                    fontFamily = SacredSerif,
                    fontSize = 28.sp,
                    color = ByzantineGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A G A P E",
                    fontFamily = SacredSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    letterSpacing = 6.sp,
                    color = ParchmentLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Heilige Schrift • Agpeya • Traditie der Kerkvaders",
                    style = MaterialTheme.typography.labelLarge,
                    color = ParchmentLight.copy(alpha = 0.8f)
                )
            }
        }
    }
}
