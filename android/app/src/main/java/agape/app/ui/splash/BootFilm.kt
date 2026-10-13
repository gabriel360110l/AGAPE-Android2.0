package agape.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import agape.app.store.LocalStore
import agape.app.theme.LocalReduceMotion
import agape.app.ui.film.QuietFilm

@Composable
fun BootFilm(onReady: () -> Unit) {
    val store = LocalStore.current
    val reduce = LocalReduceMotion.current
    val returning = rememberSaveable { store.splashSeen }
    var finished by remember { mutableStateOf(false) }
    val latestReady by rememberUpdatedState(onReady)
    val finish = {
        if (!finished) {
            finished = true
            store.markSplashSeen()
            latestReady()
        }
    }
    LaunchedEffect(reduce) { if (reduce) finish() }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        QuietFilm(if (returning) "boot-return" else "boot-first", loop = false, reduceMotion = reduce, onEnd = finish)
        TextButton(onClick = finish,
            modifier = Modifier.align(Alignment.BottomEnd).safeDrawingPadding().padding(16.dp)
                .background(Color.Black.copy(alpha = 0.55f), androidx.compose.foundation.shape.CircleShape)) {
            Text(store.t("skipOpening"), color = Color.White)
        }
    }
}
