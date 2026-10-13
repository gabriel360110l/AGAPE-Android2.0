package agape.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import agape.app.data.BookMeta
import agape.app.data.MarkTint
import agape.app.store.LocalStore
import agape.app.theme.AppColor
import agape.app.theme.AppRadius
import agape.app.theme.AppSpacing
import agape.app.theme.AppType
import agape.app.theme.AppTypography
import agape.app.theme.Haptics
import agape.app.theme.LocalReduceMotion

@Composable
fun Modifier.appColumn(): Modifier = this
    .fillMaxWidth()
    .padding(horizontal = AppSpacing.horizontal)

@Composable
fun AppScreenColumn(
    spacing: Dp = AppSpacing.section,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .appColumn()
            .padding(top = AppSpacing.xl, bottom = AppSpacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) { content() }
}

@Composable
fun Kicker(text: String, color: Color = AppColor.muted()) {
    Text(
        text.uppercase(),
        style = AppType.kicker().copy(letterSpacing = 2.2.sp),
        color = color,
        maxLines = 2
    )
}

@Composable
fun ScreenHeader(kicker: String, title: String, subtitle: String? = null, detail: String? = null) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.title)) {
        if (!kicker.equals(title, ignoreCase = true)) Kicker(kicker)
        Text(title, style = AppType.display(34.sp), color = AppColor.fg())
        if (subtitle != null) Text(subtitle, style = AppType.serif(20.sp), color = AppColor.accent())
        if (detail != null) Text(detail, style = AppType.sans(15.sp, FontWeight.Normal), color = AppColor.muted(), lineHeight = 22.sp)
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Kicker(title)
        Spacer(Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(
                action,
                style = AppType.sans(13.sp, FontWeight.Medium),
                color = AppColor.muted(),
                modifier = Modifier.clickable(onClick = onAction).padding(vertical = 10.dp)
            )
        }
    }
}

@Composable
fun SurfaceShape(radius: Dp = AppRadius.large, stroke: Boolean = true, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier
            .clip(shape)
            .background(AppColor.surface())
            .then(if (stroke) Modifier.border(1.dp, AppColor.hairline(), shape) else Modifier)
    )
}

@Composable
fun AgapeCard(padding: Dp = AppSpacing.cardPad, content: @Composable () -> Unit) {
    val shape = AppRadius.largeShape()
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColor.surface())
            .border(1.dp, AppColor.hairline(), shape)
            .padding(padding)
    ) { content() }
}

@Composable
fun AppDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColor.line()))
}

@Composable
fun MeaningCard(
    kicker: String? = null,
    term: String,
    original: String? = null,
    translit: String? = null,
    meaning: String,
    compact: Boolean = false,
    chrome: Boolean = true
) {
    val inner = @Composable {
        Column(verticalArrangement = Arrangement.spacedBy(if (compact) AppSpacing.sm else AppSpacing.md)) {
            if (!kicker.isNullOrEmpty()) Kicker(kicker)
            Text(term, style = if (compact) AppType.display(26.sp) else AppType.display(34.sp), color = AppColor.fg())
            val hasOrigin = !original.isNullOrEmpty() || !translit.isNullOrEmpty()
            if (hasOrigin) {
                Row {
                    if (!original.isNullOrEmpty()) Text(original, style = AppType.serif(18.sp), color = AppColor.accent())
                    if (!translit.isNullOrEmpty()) {
                        Spacer(Modifier.width(AppSpacing.sm))
                        Text(translit, style = AppType.serifItalic(16.sp), color = AppColor.muted())
                    }
                }
            }
            Box(
                Modifier
                    .padding(vertical = AppSpacing.xxs)
                    .width(28.dp)
                    .height(1.dp)
                    .background(AppColor.dotGold.copy(alpha = 0.45f))
            )
            Text(
                meaning,
                style = AppType.serif(if (compact) 17.sp else 19.sp),
                color = if (compact) AppColor.muted() else AppColor.fg(),
                lineHeight = if (compact) 21.sp else 25.sp
            )
        }
    }
    if (chrome) AgapeCard(padding = if (compact) AppSpacing.cardPad else AppSpacing.xl) { inner() } else inner()
}

@Composable
fun AppListRow(
    title: String,
    subtitle: String? = null,
    showChevron: Boolean = true,
    showSeparator: Boolean = true,
    emphasize: Boolean = false
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = AppTypography.body(), color = if (emphasize) AppColor.dotGold else AppColor.fg(), maxLines = 2)
                if (!subtitle.isNullOrEmpty()) {
                    Text(subtitle, style = AppTypography.caption(), color = AppColor.muted(), maxLines = 2)
                }
            }
            if (showChevron) {
                Icon(Icons.Outlined.ChevronRight, null, tint = AppColor.faint(), modifier = Modifier.size(18.dp))
            }
        }
        if (showSeparator) AppDivider()
    }
}

@Composable
fun ReadingRow(category: String, title: String, read: Boolean = false, showSeparator: Boolean = true) {
    val store = LocalStore.current
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    Kicker(category)
                    if (read) {
                        Icon(Icons.Filled.CheckCircle, store.t("readDone"), tint = AppColor.dotGold, modifier = Modifier.size(10.dp))
                    }
                }
                Text(title, style = AppTypography.headline(), color = AppColor.fg(), maxLines = 2)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = AppColor.faint(), modifier = Modifier.padding(top = 14.dp).size(16.dp))
        }
        if (showSeparator) AppDivider()
    }
}

@Composable
fun SearchSuggestionRow(title: String, showSeparator: Boolean = true, visible: Boolean = true, delayMs: Int = 0) {
    val reduce = LocalReduceMotion.current
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(320, delayMillis = if (reduce) 0 else delayMs), label = "sug")
    val y by animateFloatAsState(if (visible) 0f else 8f, tween(320, delayMillis = if (reduce) 0 else delayMs), label = "sugy")
    Column(Modifier.fillMaxWidth().alpha(alpha).offset(y = y.dp)) {
        Text(
            title,
            style = AppType.serifItalic(20.sp),
            color = AppColor.muted(),
            maxLines = 2,
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        )
        if (showSeparator) Box(Modifier.fillMaxWidth().height(1.dp).background(AppColor.line().copy(alpha = 0.45f)))
    }
}

@Composable
fun SearchResultRow(reference: String, excerpt: String, highlight: String? = null, showSeparator: Boolean = true) {
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(vertical = AppSpacing.md), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(reference, style = AppTypography.caption(), color = AppColor.accent())
            Text(excerpt, style = AppTypography.body(), color = AppColor.fg(), maxLines = 4)
        }
        if (showSeparator) AppDivider()
    }
}

@Composable
fun StatRow(items: List<Pair<String, String>>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            Column(
                Modifier.weight(1f).clip(AppRadius.largeShape())
                    .background(AppColor.surface()).border(1.dp, AppColor.hairline(), AppRadius.largeShape())
                    .padding(horizontal = 12.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(item.first, style = AppType.display(23.sp), color = AppColor.accent())
                Text(item.second, style = AppTypography.caption(), color = AppColor.muted())
            }
        }
    }
}

enum class AgapeButtonKind { primary, outline, ghost, ink, paper }

@Composable
fun AgapeButton(title: String, kind: AgapeButtonKind = AgapeButtonKind.primary, onClick: () -> Unit) {
    val bg = when (kind) {
        AgapeButtonKind.primary -> AppColor.accent()
        AgapeButtonKind.ink -> AppColor.ink
        else -> Color.Transparent
    }
    val fg = when (kind) {
        AgapeButtonKind.primary -> AppColor.accentFg()
        AgapeButtonKind.outline -> AppColor.fg()
        AgapeButtonKind.ghost -> AppColor.muted()
        AgapeButtonKind.ink -> AppColor.paper
        AgapeButtonKind.paper -> AppColor.ink
    }
    val shape = AppRadius.mediumShape()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        Modifier
            .scale(if (pressed) 0.98f else 1f)
            .alpha(if (pressed) 0.92f else 1f)
            .clip(shape)
            .background(bg)
            .then(
                if (kind == AgapeButtonKind.outline || kind == AgapeButtonKind.paper)
                    Modifier.border(1.dp, if (kind == AgapeButtonKind.paper) AppColor.ink.copy(0.16f) else AppColor.fg().copy(0.16f), shape)
                else Modifier
            )
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = AppSpacing.md)
            .heightIn(min = AppSpacing.control).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = AppType.sans(15.sp), color = fg)
    }
}

@Composable
fun PrimaryChip(title: String, filled: Boolean, onPhoto: Boolean = false) {
    val fill = if (onPhoto) AppColor.onPhoto else AppColor.accent()
    val text = if (onPhoto) AppColor.dusk else if (filled) AppColor.accentFg() else AppColor.fg()
    val shape = AppRadius.mediumShape()
    Text(
        title,
        style = AppType.sans(15.sp),
        color = if (filled) text else if (onPhoto) AppColor.onPhoto else AppColor.fg(),
        modifier = Modifier
            .clip(shape)
            .background(if (filled) fill else Color.Transparent)
            .then(
                if (!filled) Modifier.border(1.dp, (if (onPhoto) AppColor.onPhoto else AppColor.fg()).copy(0.28f), shape)
                else Modifier
            )
            .padding(horizontal = AppSpacing.md)
            .height(AppSpacing.control)
            .padding(vertical = 12.dp)
    )
}

@Composable
fun GlassIconButton(icon: ImageVector, label: String, onPhoto: Boolean = false, onClick: () -> Unit) {
    val fg = (if (onPhoto) AppColor.onPhoto else AppColor.fg()).copy(0.92f)
    Box(
        Modifier
            .size(AppSpacing.control)
            .clip(CircleShape)
            .background((if (onPhoto) AppColor.dusk else AppColor.surface()).copy(0.45f))
            .border(1.dp, if (onPhoto) AppColor.onPhoto.copy(0.22f) else AppColor.hairline(), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, label, tint = fg, modifier = Modifier.size(17.dp))
    }
}

@Composable
fun GlassScrollMenu(options: List<Pair<String, String>>, selection: String, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .fillMaxWidth()
            .nativeGlass(shape)
            .horizontalScroll(rememberScrollState())
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (id, title) ->
            val selected = selection == id
            Text(
                title,
                style = AppType.sans(14.sp, if (selected) FontWeight.SemiBold else FontWeight.Medium),
                color = if (selected) AppColor.fg() else AppColor.muted(),
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) AppColor.fg().copy(0.12f) else Color.Transparent)
                    .clickable {
                        Haptics.select(context)
                        onSelect(id)
                    }
                    .padding(horizontal = AppSpacing.md, vertical = 10.dp)
            )
        }
    }
}

@Composable
fun AppSearchField(placeholder: String, text: String, onText: (String) -> Unit) {
    val shape = AppRadius.largeShape()
    var focused = remember { androidx.compose.runtime.mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .height(AppSpacing.field)
            .clip(shape)
            .background(AppColor.surface())
            .border(1.dp, AppColor.hairline(), shape)
            .then(if (focused.value) Modifier.border(1.dp, AppColor.fg().copy(0.18f), shape) else Modifier)
            .padding(horizontal = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, null, tint = if (focused.value) AppColor.accent() else AppColor.muted(), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(AppSpacing.sm))
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) {
                Text(placeholder, style = AppType.serifItalic(18.sp), color = AppColor.faint())
            }
            BasicTextField(
                value = text,
                onValueChange = onText,
                textStyle = AppType.serif(18.sp).copy(color = AppColor.fg()),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused.value = it.isFocused }
            )
        }
        if (text.isNotEmpty()) {
            Icon(
                Icons.Filled.Close, LocalStore.current.t("clearSearch"), tint = AppColor.muted(),
                modifier = Modifier.size(48.dp).clickable { onText("") }.padding(14.dp)
            )
        }
    }
}

@Composable
fun EmptyStateView(title: String, bodyText: String, icon: ImageVector) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xxxl, horizontal = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Icon(icon, null, tint = AppColor.muted(), modifier = Modifier.size(28.dp))
        Text(title, style = AppType.display(26.sp), color = AppColor.fg())
        Text(bodyText, style = AppType.sans(15.sp, FontWeight.Normal), color = AppColor.muted(), lineHeight = 22.sp)
    }
}

@Composable
fun ArtView(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bmp = remember(name) {
        runCatching { context.assets.open("Art/$name.jpg").use { BitmapFactory.decodeStream(it) } }.getOrNull()
    }
    if (bmp != null) {
        Image(bmp.asImageBitmap(), null, modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    } else {
        Box(modifier.fillMaxSize().background(AppColor.surface()))
    }
}

@Composable
fun HeroBand(art: String, minHeight: Dp = 360.dp, content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val topInset = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
        val bandHeight = minHeight + topInset
        Box(Modifier.fillMaxWidth().heightIn(min = bandHeight), contentAlignment = Alignment.BottomStart) {
            ArtView(art, Modifier.matchParentSize())
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        0f to AppColor.dusk.copy(0.34f),
                        0.16f to AppColor.dusk.copy(0.12f),
                        0.40f to AppColor.dusk.copy(0.22f),
                        0.62f to AppColor.dusk.copy(0.55f),
                        0.80f to AppColor.dusk.copy(0.78f),
                        0.93f to AppColor.dusk.copy(0.92f),
                        1f to AppColor.dusk
                    )
                )
            )
            Box(Modifier.appColumn().padding(top = topInset + AppSpacing.sm, bottom = AppSpacing.xl)) {
                content()
            }
        }
    }
}

@Composable
fun BibleBookCard(book: BookMeta, localizedName: String, readCount: Int) {
    val store = LocalStore.current
    val shape = AppRadius.largeShape()
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColor.surface())
            .border(1.dp, AppColor.hairline(), shape)
            .padding(AppSpacing.cardPad),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.title)
    ) {
        Row {
            Kicker(if (book.greek.isEmpty()) book.shortName else book.greek)
            Spacer(Modifier.weight(1f))
            Text("${book.chapters}", style = AppType.sans(12.sp, FontWeight.Medium), color = AppColor.muted())
        }
        Text(localizedName, style = AppType.display(26.sp), color = AppColor.fg())
        Text(store.catalog.bookTeaser(book, store.state.locale), style = AppType.serifItalic(16.sp), color = AppColor.muted(), maxLines = 2)
        if (readCount > 0) {
            Row(Modifier.padding(top = AppSpacing.xxs), verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { readCount.toFloat() / maxOf(book.chapters, 1) },
                    modifier = Modifier.weight(1f).height(3.dp),
                    color = AppColor.accent(),
                    trackColor = AppColor.line()
                )
                Spacer(Modifier.width(AppSpacing.sm))
                Text("$readCount/${book.chapters}", style = AppType.sans(11.sp, FontWeight.Medium), color = AppColor.muted())
            }
        }
    }
}

@Composable
fun CalendarCell(day: Int, isToday: Boolean, rank: String, inMonth: Boolean) {
    val color = when (rank) {
        "great" -> AppColor.dotGold
        "major" -> AppColor.accent()
        else -> AppColor.fg()
    }
    val dot = when (rank) {
        "great" -> AppColor.dotGold
        "major" -> AppColor.dotAmber
        "minor" -> AppColor.dotSky
        else -> Color.Transparent
    }
    Column(
        Modifier.fillMaxWidth().height(48.dp).alpha(if (inMonth) 1f else 0.28f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            Modifier
                .size(32.dp)
                .then(if (isToday) Modifier.clip(CircleShape).background(AppColor.accent().copy(0.16f)).border(1.dp, AppColor.accent().copy(0.45f), CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text("$day", style = AppType.sans(14.sp, if (isToday) FontWeight.SemiBold else FontWeight.Normal), color = color)
        }
        Box(Modifier.size(4.dp).clip(CircleShape).background(if (rank == "ordinary" || rank.isEmpty()) Color.Transparent else dot))
    }
}

@Composable
fun VerseActionBar(
    paper: Boolean,
    onHighlight: (MarkTint) -> Unit,
    onClear: () -> Unit,
    onBookmark: () -> Unit,
    onNote: () -> Unit,
    onShare: () -> Unit,
    bookmarkTitle: String,
    noteTitle: String,
    shareTitle: String,
    highlightTitle: String,
    bookmarked: Boolean
) {
    val shape = AppRadius.extraLargeShape()
    val fg = if (paper) AppColor.ink else AppColor.fg()
    Column(Modifier.fillMaxWidth().nativeGlass(shape).padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MarkTint.entries.forEach { tint ->
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(tint.dotColor()).clickable { onHighlight(tint) }
                )
            }
            Box(Modifier.size(28.dp).clip(CircleShape).border(1.dp, fg.copy(0.3f), CircleShape).clickable(onClick = onClear))
        }
        Row(Modifier.fillMaxWidth()) {
            Action(if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, bookmarkTitle, fg, onBookmark, Modifier.weight(1f))
            Action(Icons.Outlined.Notes, noteTitle, fg, onNote, Modifier.weight(1f))
            Action(Icons.Outlined.Share, shareTitle, fg, onShare, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Action(icon: ImageVector, title: String, fg: Color, onClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, title, tint = fg, modifier = Modifier.size(16.dp))
        Text(title, style = AppType.sans(10.sp, FontWeight.Medium), color = fg, maxLines = 1)
    }
}

fun MarkTint.dotColor() = when (this) {
    MarkTint.cream -> AppColor.dotCream
    MarkTint.green -> AppColor.dotGreen
    MarkTint.gold -> AppColor.dotGold
    MarkTint.sky -> AppColor.dotSky
    MarkTint.amber -> AppColor.dotAmber
    MarkTint.rose -> AppColor.dotRose
}

fun MarkTint.fillColor() = when (this) {
    MarkTint.cream -> AppColor.markCream
    MarkTint.green -> AppColor.markGreen
    MarkTint.gold -> AppColor.markGold
    MarkTint.sky -> AppColor.markSky
    MarkTint.amber -> AppColor.markAmber
    MarkTint.rose -> AppColor.markRose
}

@Composable
fun Modifier.nativeGlass(shape: Shape): Modifier {
    return this
        .clip(shape)
        .background(AppColor.surface().copy(alpha = 0.98f))
        .border(1.dp, AppColor.fg().copy(alpha = 0.10f), shape)
}

@Composable
fun CrossMark(modifier: Modifier = Modifier) {
    val accent = AppColor.accent()
    Canvas(modifier.size(18.dp)) {
        val mid = Offset(size.width / 2, size.height / 2)
        drawLine(accent, Offset(mid.x, size.height * 0.12f), Offset(mid.x, size.height * 0.88f), 1.4.dp.toPx())
        drawLine(accent, Offset(size.width * 0.16f, mid.y), Offset(size.width * 0.84f, mid.y), 1.4.dp.toPx())
    }
}
