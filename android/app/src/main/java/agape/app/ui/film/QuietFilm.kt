package agape.app.ui.film

import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun QuietFilm(name: String, loop: Boolean, reduceMotion: Boolean, onEnd: () -> Unit = {}) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val latestEnd by rememberUpdatedState(onEnd)
    var file by remember(name) { mutableStateOf<File?>(null) }
    var view by remember(name) { mutableStateOf<VideoView?>(null) }
    var aspect by remember(name) { mutableFloatStateOf(9f / 16f) }
    var resumePlayback by remember(name) { mutableStateOf(false) }
    LaunchedEffect(name, reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                // A new app version must never reuse an older opening video.
                @Suppress("DEPRECATION")
                val version = context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
                val dest = File(context.cacheDir, "$name-$version.mp4")
                if (!dest.exists() || dest.length() == 0L) {
                    val temp = File(context.cacheDir, "$name-$version.tmp")
                    context.assets.open("films/$name.mp4").use { input ->
                        temp.outputStream().use { input.copyTo(it) }
                    }
                    check(temp.renameTo(dest)) { "Unable to prepare video" }
                }
                dest
            }.getOrNull()
        }
        if (loaded == null) latestEnd() else file = loaded
    }
    DisposableEffect(owner, view, reduceMotion) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> { resumePlayback = view?.isPlaying == true; view?.pause() }
                Lifecycle.Event.ON_RESUME -> if (resumePlayback && !reduceMotion) view?.start()
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        val width = minOf(maxWidth, maxHeight * aspect)
        val height = width / aspect
        val preparedFile = file
        if (!reduceMotion && preparedFile != null) key(name, preparedFile.path) {
            AndroidView(
                modifier = Modifier.size(width, height),
                factory = { ctx -> VideoView(ctx).apply {
                    view = this
                    setOnPreparedListener { player ->
                        if (player.videoWidth > 0 && player.videoHeight > 0) aspect = player.videoWidth.toFloat() / player.videoHeight
                        player.isLooping = loop
                        if (loop) player.setVolume(0f, 0f)
                        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) start()
                        else resumePlayback = true
                    }
                    setOnCompletionListener { if (!loop) latestEnd() }
                    setOnErrorListener { _, _, _ -> latestEnd(); true }
                    setVideoPath(preparedFile.absolutePath)
                } },
                onRelease = { it.stopPlayback(); view = null }
            )
        }
    }
}
