package agape.app.ui.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import agape.app.data.AppLocale
import agape.app.store.LocalStore
import agape.app.theme.AppType
import agape.app.theme.LocalReduceMotion
import agape.app.ui.film.QuietFilm

private data class GuidePage(val film: String, val titleNL: String, val titleEN: String, val bodyNL: String, val bodyEN: String)

private val pages = listOf(
    GuidePage("guide-east", "Richting het oosten", "Face the east", "Ga rustig voor een icoon staan, met je gezicht naar het oosten.", "Stand quietly before an icon, your face turned toward the east."),
    GuidePage("guide-cross", "Het kruisteken", "The sign of the cross", "Volg de gewoonte van je kerk. In de Byzantijnse traditie: duim, wijs- en middelvinger samen; voorhoofd, borst, rechterschouder, linkerschouder.", "Follow the custom of your church. In the Byzantine tradition: join thumb, index and middle finger; touch forehead, chest, right shoulder, then left shoulder."),
    GuidePage("guide-bow", "Het Drieheilig Gebed", "The Trisagion", "Bid driemaal, met een buiging: Heilige God, Heilige Sterke, Heilige Onsterfelijke, ontferm U over ons.", "Pray three times, with a bow: Holy God, Holy Mighty, Holy Immortal, have mercy on us."),
    GuidePage("guide-ourfather", "Het Onzevader", "The Our Father", "Bid het Onzevader rustig, met geopende of gevouwen handen.", "Pray the Our Father quietly, with open or folded hands."),
    GuidePage("guide-jesus", "Het Jezusgebed", "The Jesus Prayer", "Herhaal in stilte: Heer Jezus Christus, Zoon van God, ontferm U over mij, de zondaar.", "Repeat in silence: Lord Jesus Christ, Son of God, have mercy on me, a sinner."),
    GuidePage("guide-names", "Dank en voorbede", "Thanks and intercession", "Dank God in eigen woorden en noem de namen van je naasten, zieken en overledenen.", "Thank God in your own words, and name your loved ones, the sick, and the departed."),
    GuidePage("guide-kneel", "Afsluiting", "Closing", "Sluit af met een kruisteken. Maak een buiging of kniel, als dat voor jou mogelijk is.", "Close with the sign of the cross. Bow or kneel, if you are able.")
)

@Composable
fun PrayerTutorialScreen() {
    val store = LocalStore.current
    val en = store.state.locale == AppLocale.en
    val pager = rememberPagerState { pages.size }
    val reduce = LocalReduceMotion.current
    HorizontalPager(pager, Modifier.fillMaxSize().background(Color(0xFF0C0B0A))) { index ->
        val page = pages[index]
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            Box(Modifier.fillMaxWidth().height(280.dp)) {
                QuietFilm(page.film, loop = true, reduceMotion = reduce)
            }
            Text("${index + 1} / ${pages.size} · " + if (en) "Swipe to continue" else "Veeg om verder te gaan",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                style = AppType.sans(13.sp), color = Color(0xFFECC077))
            Text(if (en) page.titleEN else page.titleNL, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp), style = AppType.serifItalic(26.sp), color = Color.White)
            Text(if (en) page.bodyEN else page.bodyNL, modifier = Modifier.padding(horizontal = 24.dp), style = AppType.serif(17.sp), color = Color.White.copy(0.82f))
        }
    }
}
