package agape.app.store

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import agape.app.data.AppLocale
import agape.app.data.BibleDatabase
import agape.app.data.ChapterRef
import agape.app.data.ChurchCalendar
import agape.app.data.ContentCatalog
import agape.app.data.HistoryItem
import agape.app.data.MarkTint
import agape.app.data.SavedVerse
import agape.app.data.TranslationID
import agape.app.data.UserState
import agape.app.data.VerseRecord
import agape.app.theme.Haptics
import kotlinx.serialization.json.Json
import java.io.File
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

val LocalStore = staticCompositionLocalOf<AppStore> { error("AppStore missing") }

class AppStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val file = File(context.filesDir, "agape-user.json")
    private val prefs = context.getSharedPreferences("agape", Context.MODE_PRIVATE)

    var state by mutableStateOf(load())
        private set
    var seenWelcome by mutableStateOf(prefs.getBoolean("seenWelcome", false))
        private set
    var splashSeen by mutableStateOf(prefs.getBoolean("splashSeen", false))
        private set
    var activeJourneyId by mutableStateOf(prefs.getString("activeJourney", null))
        private set
    var journeyStreak by mutableStateOf(prefs.getInt("journeyStreak", 0))
        private set

    val catalog get() = ContentCatalog
    val night get() = ChurchCalendar.effectiveNight(state.nightMode)

    fun t(key: String) = ContentCatalog.t(key, state.locale)

    fun update(block: UserState.() -> UserState) {
        state = block(state)
        persistSoon()
    }

    fun persist() {
        Haptics.isEnabled = state.hapticsEnabled
        file.writeText(json.encodeToString(UserState.serializer(), state))
    }

    private fun persistSoon() {
        persist()
    }

    fun applyLocale(raw: String) {
        val locale = AppLocale.entries.find { it.name == raw } ?: AppLocale.en
        state = state.copy(
            locale = locale,
            translation = if (locale == AppLocale.en) TranslationID.kjv else if (state.translation == TranslationID.kjv) TranslationID.sv else state.translation
        )
        persist()
        refreshAlarms()
    }

    fun displayedVerse(bookId: String, chapter: Int, verse: Int, fallback: String? = null): String {
        val record = BibleDatabase.verse(bookId, chapter, verse)
        if (record != null) {
            val text = verseText(record)
            if (text.isNotEmpty()) return text
        }
        if (state.locale == AppLocale.en) return ""
        return fallback.orEmpty()
    }

    fun displayedRef(bookId: String, chapter: Int, verse: Int? = null): String {
        val name = bookName(bookId)
        return if (verse != null) "$name $chapter:$verse" else "$name $chapter"
    }

    fun bookName(id: String): String {
        val book = catalog.book(id) ?: return id
        return catalog.bookName(book, state.locale)
    }

    fun readCount(bookId: String) = state.read[bookId]?.size ?: 0
    fun bookProgress(chapters: Int, bookId: String): Double {
        if (chapters <= 0) return 0.0
        return readCount(bookId).toDouble() / chapters.toDouble()
    }

    fun markVisited(bookId: String, chapter: Int) {
        val loc = if (state.locale == AppLocale.en) Locale.UK else Locale("nl", "NL")
        val at = DateFormat.getDateInstance(DateFormat.MEDIUM, loc).format(java.util.Date())
        val item = HistoryItem(bookId, chapter, at)
        val history = (listOf(item) + state.history.filterNot { it.bookId == bookId && it.chapter == chapter }).take(24)
        state = state.copy(last = ChapterRef(bookId, chapter), history = history)
        bumpStreak()
        persist()
    }

    fun toggleRead(bookId: String, chapter: Int) {
        val list = (state.read[bookId] ?: emptyList()).toMutableList()
        if (list.contains(chapter)) list.remove(chapter) else {
            list.add(chapter)
            bumpStreak()
        }
        state = state.copy(read = state.read + (bookId to list))
        persist()
        Haptics.light(context)
    }

    fun isRead(bookId: String, chapter: Int) = state.read[bookId]?.contains(chapter) == true
    fun chaptersReadCount() = catalog.books(state.tradition).sumOf { state.read[it.id]?.size ?: 0 }
    fun totalChapters() = catalog.books(state.tradition).sumOf { it.chapters }
    fun clearRead() {
        state = state.copy(read = emptyMap())
        persist()
    }

    fun setFont(delta: Double) {
        state = state.copy(fontScale = (state.fontScale + delta).coerceIn(0.9, 1.35))
        persist()
    }

    fun toggleSave(item: SavedVerse) {
        val saved = state.saved.toMutableList()
        val i = saved.indexOfFirst { it.key == item.key }
        if (i >= 0) saved.removeAt(i) else {
            saved.add(0, item)
            Haptics.success(context)
        }
        state = state.copy(saved = saved)
        persist()
    }

    fun isSaved(key: String) = state.saved.any { it.key == key }

    fun setColor(key: String, color: MarkTint?, snippet: SavedVerse?) {
        val colors = state.colors.toMutableMap()
        val snippets = state.snippets.toMutableMap()
        if (color != null) {
            colors[key] = color
            if (snippet != null) snippets[key] = snippet
            Haptics.light(context)
        } else {
            colors.remove(key)
            snippets.remove(key)
        }
        state = state.copy(colors = colors, snippets = snippets)
        persist()
    }

    fun setNote(key: String, note: String) {
        val notes = state.notes.toMutableMap()
        if (note.trim().isEmpty()) notes.remove(key) else notes[key] = note
        state = state.copy(notes = notes)
        persist()
    }

    fun refreshPrayerDay() {
        val today = ChurchCalendar.ymd()
        if (state.prayerTodayYmd == today) return
        state = state.copy(prayerTodayYmd = today, prayerTodayCount = 0)
        persist()
    }

    fun incPrayer() {
        refreshPrayerDay()
        state = state.copy(prayerCount = state.prayerCount + 1, prayerTodayCount = state.prayerTodayCount + 1)
        persist()
        Haptics.light(context)
    }

    fun advancePrayerSession(id: String) {
        val next = agape.app.data.PrayerSession.advance(state, id, ChurchCalendar.ymd())
        if (next === state) return
        state = next
        persist()
        if (state.prayerSessions[id] == agape.app.data.PrayerSession.TARGET) Haptics.success(context)
        else Haptics.light(context)
    }

    fun resetPrayerSession(id: String) {
        state = agape.app.data.PrayerSession.reset(state, id)
        persist()
    }

    fun resetPrayer() {
        refreshPrayerDay()
        state = state.copy(prayerTodayCount = 0)
        persist()
    }

    fun markSplashSeen() {
        splashSeen = true
        prefs.edit().putBoolean("splashSeen", true).apply()
    }

    fun saveProfileName(name: String) {
        state = state.copy(profileName = name.trim().take(60))
        persist()
    }

    fun completeWelcome(name: String? = null) {
        seenWelcome = true
        prefs.edit().putBoolean("seenWelcome", true).apply()
        state = state.copy(seenIntro = true, profileName = name?.trim()?.take(60) ?: state.profileName)
        persist()
    }

    fun journeyCursor(id: String, count: Int): Int {
        val done = state.planDone[id]?.toSet().orEmpty()
        return (0 until count).firstOrNull { it !in done } ?: count
    }

    fun enrolledJourneys(): List<String> = prefs.getStringSet("enrolledJourneys", emptySet())?.toList().orEmpty()

    fun isEnrolled(id: String): Boolean {
        if (id in enrolledJourneys()) return true
        return state.planDone[id].orEmpty().isNotEmpty()
    }

    fun enroll(id: String) {
        val gate = ChurchCalendar.journeyGate(id, state.calendarRite, state.locale)
        if (gate != null && !gate.open) return
        val set = enrolledJourneys().toMutableSet()
        set.add(id)
        prefs.edit().putStringSet("enrolledJourneys", set).apply()
        openJourney(id)
        Haptics.success(context)
    }

    fun openJourney(id: String) {
        activeJourneyId = id
        prefs.edit().putString("activeJourney", id).apply()
    }

    fun completeJourneyStep(id: String, index: Int, count: Int, bookId: String, chapter: Int) {
        if (index != journeyCursor(id, count)) return
        val list = (state.planDone[id].orEmpty() + index)
        state = state.copy(planDone = state.planDone + (id to list))
        openJourney(id)
        markVisited(bookId, chapter)
        if (!isRead(bookId, chapter)) toggleRead(bookId, chapter)
        journeyStreak = recordJourneyStreak()
        persist()
        Haptics.success(context)
    }

    private fun recordJourneyStreak(): Int {
        val today = ChurchCalendar.ymd()
        val last = prefs.getString("journeyLastDay", null)
        val current = prefs.getInt("journeyStreak", 0)
        val yesterday = ChurchCalendar.ymd(ChurchCalendar.addDays(System.currentTimeMillis(), -1))
        val next = when (last) {
            today -> maxOf(current, 1)
            yesterday -> current + 1
            else -> 1
        }
        prefs.edit().putInt("journeyStreak", next).putString("journeyLastDay", today).apply()
        return next
    }

    fun togglePlan(planId: String, day: Int) {
        val list = (state.planDone[planId] ?: emptyList()).toMutableList()
        if (list.contains(day)) list.remove(day) else list.add(day)
        state = state.copy(planDone = state.planDone + (planId to list))
        persist()
        Haptics.light(context)
    }

    fun pushQuery(q: String) {
        val trimmed = q.trim()
        if (trimmed.length < 2) return
        val recent = (listOf(trimmed) + state.recentQueries.filterNot { it.equals(trimmed, true) }).take(12)
        state = state.copy(recentQueries = recent)
        persist()
    }

    val readingTranslation: TranslationID
        get() = if (state.locale == AppLocale.en) TranslationID.kjv else if (state.translation == TranslationID.kjv) TranslationID.sv else state.translation

    fun verseText(record: VerseRecord) = record.text(readingTranslation)

    private fun bumpStreak() {
        val today = ChurchCalendar.ymd()
        if (state.lastReadDay == today) return
        val yesterday = ChurchCalendar.ymd(ChurchCalendar.addDays(System.currentTimeMillis(), -1))
        val streak = if (state.lastReadDay == yesterday) maxOf(1, state.streak) + 1 else 1
        state = state.copy(streak = streak, lastReadDay = today)
    }

    fun refreshAlarms() {
        PrayerAlarms.sync(context, this)
    }

    private fun load(): UserState {
        val decoded = try {
            if (file.exists()) json.decodeFromString(UserState.serializer(), file.readText()) else null
        } catch (_: Exception) { null }
        return if (decoded != null) decoded else {
            val hours = ContentCatalog.hours.associate { it.id to it.alarm }
            UserState(alarmHours = hours)
        }
    }
}

object PrayerAlarms {
    fun sync(context: Context, store: AppStore) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        ContentCatalog.hours.forEach { hour ->
            val intent = Intent(context, agape.app.data.PrayerAlarmReceiver::class.java).apply {
                putExtra("hourId", hour.id)
            }
            val pi = PendingIntent.getBroadcast(
                context, hour.id.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
            val enabled = store.state.prayerAlarms && (store.state.alarmHours[hour.id] ?: hour.alarm)
            if (!enabled) return@forEach
            var minute = hour.minute - 5
            var h = hour.hour
            if (minute < 0) {
                minute += 60
                h = (h + 23) % 24
            }
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, h)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1)
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
            } else {
                am.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
            }
        }
    }
}
