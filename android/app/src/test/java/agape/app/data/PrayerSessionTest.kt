package agape.app.data

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class PrayerSessionTest {
    private val today = "2026-10-05"

    @Test fun fortyOneRepetitionsStopAndExtraTapsDoNotChangeTotals() {
        var state = UserState()
        repeat(41) { state = PrayerSession.advance(state, "jesus", today) }
        assertEquals(41, state.prayerSessions["jesus"])
        assertEquals(41, state.prayerTodayCount)
        assertEquals(41, state.prayerCount)
        assertSame(state, PrayerSession.advance(state, "jesus", today))
    }

    @Test fun roundsStaySeparateAndResetPreservesTotals() {
        var state = PrayerSession.advance(UserState(), "morning", today)
        state = PrayerSession.advance(state, "evening", today)
        state = PrayerSession.reset(state, "morning")
        assertNull(state.prayerSessions["morning"])
        assertEquals(1, state.prayerSessions["evening"])
        assertEquals(2, state.prayerTodayCount)
        assertEquals(2, state.prayerCount)
        state = PrayerSession.advance(state, "morning", today)
        assertEquals(1, state.prayerSessions["morning"])
        assertEquals(3, state.prayerTodayCount)
    }

    @Test fun midnightResetsOnlyTheDailyTotal() {
        var state = UserState(prayerTodayYmd = "2026-10-04", prayerTodayCount = 8,
            prayerCount = 20, prayerSessions = mapOf("jesus" to 8))
        state = PrayerSession.advance(state, "jesus", today)
        assertEquals(9, state.prayerSessions["jesus"])
        assertEquals(1, state.prayerTodayCount)
        assertEquals(21, state.prayerCount)
        assertEquals(today, state.prayerTodayYmd)
    }

    @Test fun savedRoundsSurviveSerializationAndOldFilesStillLoad() {
        val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
        val state = UserState(prayerSessions = mapOf("jesus" to 17, "agpeya/prime/10" to 41))
        val loaded = json.decodeFromString(UserState.serializer(), json.encodeToString(UserState.serializer(), state))
        assertEquals(state.prayerSessions, loaded.prayerSessions)
        val legacy = json.decodeFromString(UserState.serializer(), """{"prayerCount":12,"prayerTodayCount":3}""")
        assertTrue(legacy.prayerSessions.isEmpty())
        assertEquals(12, legacy.prayerCount)
    }
}
