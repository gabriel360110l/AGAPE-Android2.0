package agape.app.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.TextStyle

val LocalNight = compositionLocalOf { false }
val LocalReduceMotion = compositionLocalOf { false }

private fun rgb(r: Int, g: Int, b: Int, a: Float = 1f) =
    Color(r / 255f, g / 255f, b / 255f, a)

object AppColor {
    private val bgL = rgb(247, 244, 237)
    private val bgD = rgb(12, 14, 18)
    private val surfaceL = rgb(255, 253, 248)
    private val surfaceD = rgb(22, 25, 31)
    private val surface2L = rgb(232, 224, 210)
    private val surface2D = rgb(32, 36, 43)
    private val fgL = rgb(28, 25, 22)
    private val fgD = rgb(247, 244, 237)
    private val mutedL = rgb(108, 100, 88)
    private val mutedD = rgb(154, 147, 136)
    private val faintL = rgb(116, 106, 91)
    private val faintD = rgb(160, 160, 163)
    private val accentL = rgb(121, 81, 29)
    private val accentD = rgb(236, 192, 119)
    private val accentFgL = rgb(247, 244, 237)
    private val accentFgD = rgb(20, 18, 16)
    private val ringL = rgb(138, 122, 96)
    private val ringD = rgb(201, 193, 179)

    val paper = rgb(247, 244, 237)
    val paperEdge = rgb(228, 220, 207)
    val ink = rgb(28, 25, 22)
    val inkSoft = rgb(63, 58, 52)
    val dusk = rgb(12, 14, 18)
    val onPhoto = rgb(247, 244, 237)
    val speech = rgb(107, 44, 40)
    val speechNight = rgb(227, 182, 174)
    val cite = rgb(142, 176, 224)
    val citeInk = rgb(47, 79, 138)
    val markCream = rgb(243, 238, 228, 0.20f)
    val markGreen = rgb(90, 150, 100, 0.34f)
    val markGold = rgb(214, 176, 70, 0.38f)
    val markSky = rgb(80, 140, 190, 0.32f)
    val markAmber = rgb(210, 120, 70, 0.34f)
    val markRose = rgb(168, 86, 78, 0.30f)
    val dotCream = rgb(247, 244, 237)
    val dotGreen = rgb(90, 154, 100)
    val dotGold = rgb(212, 176, 60)
    val dotSky = rgb(74, 138, 184)
    val dotAmber = rgb(212, 132, 74)
    val dotRose = rgb(176, 96, 88)
    val glow = rgb(232, 208, 150)

    @Composable fun bg() = if (LocalNight.current) bgD else bgL
    @Composable fun surface() = if (LocalNight.current) surfaceD else surfaceL
    @Composable fun surface2() = if (LocalNight.current) surface2D else surface2L
    @Composable fun fg() = if (LocalNight.current) fgD else fgL
    @Composable fun muted() = if (LocalNight.current) mutedD else mutedL
    @Composable fun faint() = if (LocalNight.current) faintD else faintL
    @Composable fun line() = (if (LocalNight.current) fgD else fgL).copy(alpha = 0.10f)
    @Composable fun hairline() = (if (LocalNight.current) fgD else fgL).copy(alpha = 0.08f)
    @Composable fun accent() = if (LocalNight.current) accentD else accentL
    @Composable fun accentFg() = if (LocalNight.current) accentFgD else accentFgL
    @Composable fun ring() = if (LocalNight.current) ringD else ringL
}

object AppRadius {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 28.dp
    val sm get() = small
    val md get() = medium
    val lg get() = large
    val xl get() = extraLarge
    fun largeShape() = RoundedCornerShape(large)
    fun mediumShape() = RoundedCornerShape(medium)
    fun extraLargeShape() = RoundedCornerShape(extraLarge)
}

object AppSpacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 40.dp
    val xxxxl: Dp = 64.dp
    val hero: Dp = 48.dp
    val horizontal: Dp = 20.dp
    val maxWidth: Dp = 560.dp
    val section: Dp = 32.dp
    val sectionHeader: Dp = 12.dp
    val card: Dp = 12.dp
    val cardPad: Dp = 20.dp
    val title: Dp = 8.dp
    val cluster: Dp = 8.dp
    val text: Dp = 8.dp
    val button: Dp = 8.dp
    val icon: Dp = 12.dp
    val control: Dp = 52.dp
    val field: Dp = 56.dp
}

object AppType {
    val serifFamily: FontFamily = FontFamily.Serif
    val sansFamily: FontFamily = FontFamily.SansSerif

    fun display(size: TextUnit) = TextStyle(
        fontFamily = serifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = size
    )
    fun serif(size: TextUnit) = TextStyle(
        fontFamily = serifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = size
    )
    fun serifItalic(size: TextUnit) = TextStyle(
        fontFamily = serifFamily,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = size
    )
    fun sans(size: TextUnit, weight: FontWeight = FontWeight.Medium) = TextStyle(
        fontFamily = sansFamily,
        fontWeight = weight,
        fontSize = size
    )
    fun kicker() = sans(11.sp, FontWeight.Medium)
}

object AppTypography {
    fun display() = AppType.display(34.sp)
    fun title() = AppType.display(26.sp)
    fun headline() = AppType.display(22.sp)
    fun body() = AppType.serif(18.sp)
    fun secondary() = AppType.sans(15.sp, FontWeight.Normal)
    fun caption() = AppType.sans(13.sp, FontWeight.Medium)
    fun sectionLabel() = AppType.kicker()
}

object AppAnimation {
    val quick = tween<Float>(180, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f))
    val standard = tween<Float>(280, easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f))
    val slow = tween<Float>(480, easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f))
    val spring = spring<Float>(dampingRatio = 0.84f, stiffness = Spring.StiffnessMediumLow)
    val gentle = tween<Float>(360, easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f))
}

@Composable
fun AgapeRoot(night: Boolean, content: @Composable () -> Unit) {
    val reduce = !android.animation.ValueAnimator.areAnimatorsEnabled()
    CompositionLocalProvider(
        LocalNight provides night,
        LocalReduceMotion provides reduce
    ) {
        val scheme = if (night) {
            darkColorScheme(
                background = AppColor.dusk,
                surface = rgb(22, 25, 31),
                primary = rgb(236, 192, 119),
                onPrimary = rgb(20, 18, 16),
                onBackground = rgb(247, 244, 237),
                onSurface = rgb(247, 244, 237)
            )
        } else {
            lightColorScheme(
                background = AppColor.paper,
                surface = rgb(255, 253, 248),
                primary = rgb(121, 81, 29),
                onPrimary = rgb(247, 244, 237),
                onBackground = rgb(28, 25, 22),
                onSurface = rgb(28, 25, 22)
            )
        }
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography(bodyLarge = AppType.serif(18.sp))
        ) {
            content()
        }
    }
}
