package agape.app.data

/** A saved prayer round. Daily totals count repetitions, not completed rounds. */
object PrayerSession {
    const val TARGET = 41

    fun advance(state: UserState, id: String, today: String): UserState {
        val count = (state.prayerSessions[id] ?: 0).coerceIn(0, TARGET)
        if (count == TARGET) return state
        val daily = if (state.prayerTodayYmd == today) state.prayerTodayCount else 0
        return state.copy(
            prayerSessions = state.prayerSessions + (id to count + 1),
            prayerCount = state.prayerCount + 1,
            prayerTodayCount = daily + 1,
            prayerTodayYmd = today
        )
    }

    fun reset(state: UserState, id: String): UserState =
        state.copy(prayerSessions = state.prayerSessions - id)
}
