package agape.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Sacrale Kleuren (Perkament, Wijnrood, Byzantijns Goud, Donkere Hout-/Steentinten)
val ParchmentLight = Color(0xFFFAF6EE)
val ParchmentSurface = Color(0xFFF2ECE1)
val ParchmentBorder = Color(0xFFDCD2C0)
val WalnutInk = Color(0xFF221A15)
val MutedStone = Color(0xFF6E6259)

val CopticBurgundy = Color(0xFF6B1D24)
val ByzantineGold = Color(0xFFC59B27)
val ByzantineGoldSoft = Color(0x26C59B27)

// Nachtwake / Vigilie (Dark Mode)
val VigilObsidian = Color(0xFF120F0D)
val VigilSurface = Color(0xFF1C1714)
val VigilBorder = Color(0xFF332922)
val VigilParchmentText = Color(0xFFEAE2D3)
val VigilMutedText = Color(0xFFA3968A)
val VigilCrimson = Color(0xFFA83843)
val VigilGold = Color(0xFFD4AF37)

@Immutable
data class AgapeSacredColors(
    val background: Color,
    val surface: Color,
    val border: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val rubricRed: Color,
    val sacredGold: Color,
    val goldHighlight: Color
)

val LocalAgapeColors = staticCompositionLocalOf {
    AgapeSacredColors(
        background = ParchmentLight,
        surface = ParchmentSurface,
        border = ParchmentBorder,
        primaryText = WalnutInk,
        secondaryText = MutedStone,
        rubricRed = CopticBurgundy,
        sacredGold = ByzantineGold,
        goldHighlight = ByzantineGoldSoft
    )
}

val SacredSerif = FontFamily.Serif
val CleanSans = FontFamily.SansSerif

val AgapeTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SacredSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.2.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SacredSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SacredSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 25.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SacredSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 19.sp,
        lineHeight = 31.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SacredSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp
    )
)

val AgapeShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun AgapeSacredTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val sacredColors = if (darkTheme) {
        AgapeSacredColors(
            background = VigilObsidian,
            surface = VigilSurface,
            border = VigilBorder,
            primaryText = VigilParchmentText,
            secondaryText = VigilMutedText,
            rubricRed = VigilCrimson,
            sacredGold = VigilGold,
            goldHighlight = ByzantineGoldSoft
        )
    } else {
        AgapeSacredColors(
            background = ParchmentLight,
            surface = ParchmentSurface,
            border = ParchmentBorder,
            primaryText = WalnutInk,
            secondaryText = MutedStone,
            rubricRed = CopticBurgundy,
            sacredGold = ByzantineGold,
            goldHighlight = ByzantineGoldSoft
        )
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = VigilGold,
            onPrimary = VigilObsidian,
            secondary = VigilCrimson,
            background = VigilObsidian,
            surface = VigilSurface,
            onBackground = VigilParchmentText,
            onSurface = VigilParchmentText,
            outline = VigilBorder
        )
    } else {
        lightColorScheme(
            primary = CopticBurgundy,
            onPrimary = ParchmentLight,
            secondary = ByzantineGold,
            background = ParchmentLight,
            surface = ParchmentSurface,
            onBackground = WalnutInk,
            onSurface = WalnutInk,
            outline = ParchmentBorder
        )
    }

    CompositionLocalProvider(LocalAgapeColors provides sacredColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AgapeTypography,
            shapes = AgapeShapes,
            content = content
        )
    }
}
