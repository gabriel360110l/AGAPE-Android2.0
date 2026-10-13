package agape.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import agape.app.theme.AppType
import agape.app.theme.LocalReduceMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashView(onReady: () -> Unit) {
    val reduce = LocalReduceMotion.current
    val camera = remember { Animatable(if (reduce) 1f else 0f) }
    val sky = remember { Animatable(if (reduce) 1f else 0f) }
    val letters = remember { Animatable(if (reduce) 1f else 0f) }
    val rise = remember { Animatable(if (reduce) 1f else 0f) }
    val glow = remember { Animatable(if (reduce) 1f else 0f) }
    val flow = remember { Animatable(if (reduce) 1f else 0f) }
    val open = remember { Animatable(if (reduce) 1f else 0f) }
    val light = remember { Animatable(if (reduce) 1f else 0f) }
    val drift by rememberInfiniteTransition(label = "drift").animateFloat(
        0f, 1f, infiniteRepeatable(tween(6500), RepeatMode.Reverse), label = "driftv"
    )

    LaunchedEffect(reduce) {
        if (reduce) {
            delay(420)
            onReady()
            return@LaunchedEffect
        }
        launch { camera.animateTo(1f, tween(7600, easing = LinearEasing)) }
        launch { sky.animateTo(1f, tween(2100)) }
        launch { rise.animateTo(1f, tween(1350)) }
        launch { letters.animateTo(1f, tween(1700, delayMillis = 300)) }
        launch { glow.animateTo(1f, tween(3400, delayMillis = 400)) }
        delay(1300)
        launch { flow.animateTo(1f, tween(2500)) }
        delay(2200)
        launch { open.animateTo(1f, tween(520)) }
        launch { light.animateTo(1f, tween(1600)) }
        delay(2600)
        onReady()
    }

    val word = listOf("A", "G", "A", "P", "E")
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(Modifier.fillMaxSize().alpha(sky.value)) {
            val warm = light.value
            drawRect(
                Brush.verticalGradient(
                    listOf(
                        Color(8 / 255f, 10 / 255f, 22 / 255f),
                        Color((18 + 40 * warm) / 255f, (16 + 18 * warm) / 255f, (28 + 8 * warm) / 255f),
                        Color((42 + 70 * warm) / 255f, (28 + 22 * warm) / 255f, 22 / 255f)
                    )
                )
            )
            val stars = 48
            for (i in 0 until stars) {
                val x = (i * 97f % size.width)
                val y = (i * 53f % (size.height * 0.55f))
                drawCircle(Color.White.copy(alpha = 0.18f + 0.22f * ((i % 5) / 5f) * sky.value), 1.4f + (i % 3), Offset(x, y))
            }
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .offset(y = ((1 - rise.value) * 280).dp)
        ) {
            val w = size.width
            val h = size.height
            val groundY = h * 0.78f
            val hill = Path().apply {
                moveTo(0f, h)
                lineTo(0f, groundY)
                quadraticTo(w * 0.22f, groundY - h * 0.08f, w * 0.48f, groundY - h * 0.04f)
                quadraticTo(w * 0.72f, groundY + h * 0.01f, w, groundY - h * 0.06f)
                lineTo(w, h)
                close()
            }
            drawPath(hill, Color(18 / 255f, 16 / 255f, 12 / 255f))

            fun cross(cx: Float, cy: Float, s: Float) {
                drawLine(Color(210 / 255f, 196 / 255f, 160 / 255f).copy(0.85f), Offset(cx, cy - s), Offset(cx, cy + s * 1.4f), 3.2f, StrokeCap.Round)
                drawLine(Color(210 / 255f, 196 / 255f, 160 / 255f).copy(0.85f), Offset(cx - s * 0.55f, cy - s * 0.15f), Offset(cx + s * 0.55f, cy - s * 0.15f), 3.2f, StrokeCap.Round)
            }
            cross(w * 0.46f, groundY - h * 0.18f, h * 0.045f)
            cross(w * 0.54f, groundY - h * 0.16f, h * 0.032f)
            cross(w * 0.38f, groundY - h * 0.155f, h * 0.030f)

            val tombL = w * 0.58f
            val tombT = groundY - h * 0.12f
            val tombW = w * 0.22f
            val tombH = h * 0.12f
            drawRoundRect(Color(38 / 255f, 34 / 255f, 28 / 255f), Offset(tombL, tombT), Size(tombW, tombH), CornerRadius(18f, 18f))
            val doorW = tombW * 0.42f
            val doorH = tombH * 0.78f
            drawRoundRect(Color(12 / 255f, 10 / 255f, 8 / 255f), Offset(tombL + tombW * 0.12f, tombT + tombH * 0.18f), Size(doorW, doorH), CornerRadius(8f, 8f))
            if (light.value > 0.05f) {
                drawCircle(
                    Color(255 / 255f, 236 / 255f, 180 / 255f).copy(0.35f * light.value),
                    tombH * 0.55f * light.value,
                    Offset(tombL + tombW * 0.28f, tombT + tombH * 0.55f)
                )
            }
            val stoneR = tombH * 0.38f
            val stoneX = tombL + tombW * 0.08f + open.value * tombW * 0.62f + drift * 6f
            val stoneY = tombT + tombH * 0.62f
            drawCircle(Color(62 / 255f, 56 / 255f, 46 / 255f), stoneR, Offset(stoneX, stoneY))
            drawCircle(Color(90 / 255f, 82 / 255f, 68 / 255f).copy(0.5f), stoneR * 0.7f, Offset(stoneX - 4f, stoneY - 4f))

            if (flow.value > 0f) {
                val wine = Color(120 / 255f, 28 / 255f, 32 / 255f).copy(0.55f * flow.value)
                drawLine(wine, Offset(w * 0.42f, groundY - h * 0.16f), Offset(w * 0.40f, groundY + h * 0.04f), 3.5f, StrokeCap.Round)
                drawLine(wine, Offset(w * 0.50f, groundY - h * 0.17f), Offset(w * 0.53f, groundY + h * 0.02f), 2.8f, StrokeCap.Round)
            }

            if (glow.value > 0f) {
                drawCircle(
                    Color(232 / 255f, 208 / 255f, 150 / 255f).copy(0.12f * glow.value),
                    h * 0.22f,
                    Offset(w * 0.5f, groundY - h * 0.22f)
                )
            }
        }

        val tint = Color(
            ((228f + 27f * light.value) / 255f).coerceAtMost(1f),
            ((220f + 26f * light.value) / 255f).coerceAtMost(1f),
            ((206f + 20f * light.value) / 255f).coerceAtMost(1f)
        )
        Row(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
                .padding(start = 8.dp)
        ) {
            word.forEachIndexed { index, letter ->
                val start = index * 0.11f
                val reveal = ((letters.value - start) / 0.46f).coerceIn(0f, 1f)
                Text(
                    letter,
                    style = AppType.display(42.sp),
                    color = tint,
                    modifier = Modifier
                        .width(42.dp)
                        .alpha(reveal)
                        .blur(((1 - reveal) * 7).dp)
                        .offset(y = ((1 - reveal) * 14).dp)
                )
            }
        }
    }
}
