package agape.app.data

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

object ChurchCalendar {
    const val julianOffset = 13

    fun ymd(millis: Long = System.currentTimeMillis()): String {
        val c = cal(millis)
        return "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    fun parseYmd(value: String): Long? = runCatching {
        java.time.LocalDate.parse(value).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.getOrNull()

    fun civil(y: Int, m: Int, d: Int): Long =
        java.time.LocalDate.of(y, m, d).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun addDays(millis: Long, n: Int): Long =
        java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault())
            .plusDays(n.toLong()).toInstant().toEpochMilli()

    private fun daysBetween(from: Long, to: Long): Int = java.time.temporal.ChronoUnit.DAYS.between(
        java.time.Instant.ofEpochMilli(from).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
        java.time.Instant.ofEpochMilli(to).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    ).toInt()

    fun orthodoxPascha(year: Int): Long {
        val a = year % 4
        val b = year % 7
        val c = year % 19
        val d = (19 * c + 15) % 30
        val e = (2 * a + 4 * b - d + 34) % 7
        val month = (d + e + 114) / 31
        val day = ((d + e + 114) % 31) + 1
        return addDays(civil(year, month, day), julianOffset)
    }

    fun liturgicalMonthDay(civilDate: Long, rite: CalendarRite): Pair<Int, Int> {
        val source = if (rite == CalendarRite.julian) addDays(civilDate, -julianOffset) else civilDate
        val c = cal(source)
        return (c.get(Calendar.MONTH) + 1) to c.get(Calendar.DAY_OF_MONTH)
    }

    fun civilDateForMenologion(year: Int, month: Int, day: Int, rite: CalendarRite): Long {
        val base = civil(year, month, day)
        return if (rite == CalendarRite.julian) addDays(base, julianOffset) else base
    }

    fun calendarFor(dateMillis: Long = System.currentTimeMillis(), rite: CalendarRite, locale: AppLocale = AppLocale.nl): CalendarDay {
        val c = cal(dateMillis)
        val civilDate = civil(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        val lit = liturgicalMonthDay(civilDate, rite)
        val fixed = ContentCatalog.menologion["${lit.first}-${lit.second}"] ?: fallback(lit.first, lit.second)
        val movable = movableOn(civilDate, locale)
        val around = seasonAround(civilDate, rite, locale)
        if (movable != null && (movable.rank == "great" || fixed.rank != "great")) {
            return CalendarDay(
                dateMillis = civilDate, ymd = ymd(civilDate), title = movable.title,
                saint = if (fixed.rank == "great") movable.title else fixed.title(locale),
                teaser = movable.teaser, explanation = movable.explanation,
                tone = movable.tone, season = movable.season, rank = movable.rank,
                menologionLabel = label(civilDate, rite, locale)
            )
        }
        val feast = fixed.rank == "great" || fixed.rank == "major"
        return CalendarDay(
            dateMillis = civilDate, ymd = ymd(civilDate), title = fixed.title(locale), saint = fixed.title(locale),
            teaser = around.teaser ?: fixed.teaser(locale), explanation = fixed.explanation(locale),
            tone = if (feast) "feast" else around.tone,
            season = if (feast) seasonLabel("Feest", locale) else around.season,
            rank = fixed.rank,
            menologionLabel = label(civilDate, rite, locale)
        )
    }

    fun monthGrid(year: Int, month: Int, rite: CalendarRite, locale: AppLocale = AppLocale.nl): List<CalendarDay?> {
        val first = civil(year, month, 1)
        val startDow = (cal(first).get(Calendar.DAY_OF_WEEK) + 5) % 7
        val days = cal(first).getActualMaximum(Calendar.DAY_OF_MONTH)
        val cells = MutableList<CalendarDay?>(startDow) { null }
        for (d in 1..days) cells += calendarFor(civil(year, month, d), rite, locale)
        while (cells.size % 7 != 0) cells += null
        return cells
    }

    fun seasonLabel(season: String, locale: AppLocale): String {
        if (locale != AppLocale.en) return season
        return mapOf(
            "Grote Vasten" to "Great Lent", "Pasen" to "Pascha", "Goede Week" to "Holy Week",
            "Pinksteren" to "Pentecost", "Vasten van de Theotokos" to "Dormition fast",
            "Geboortevasten" to "Nativity fast", "Apostelenvasten" to "Apostles’ fast",
            "Tijd van het jaar" to "Season of the year", "Feest" to "Feast"
        )[season] ?: season
    }

    fun effectiveNight(mode: NightMode, dateMillis: Long = System.currentTimeMillis()): Boolean {
        return when (mode) {
            NightMode.night -> true
            NightMode.day -> false
            NightMode.auto -> {
                val h = cal(dateMillis).get(Calendar.HOUR_OF_DAY)
                h >= 19 || h < 6
            }
        }
    }

    private fun fallback(month: Int, day: Int) = MenologionEntry(
        month = month, day = day, title = "Een dag voor gebed en bezinning",
        teaser = "De Kerk gedenkt de heiligen die op deze dag ontsliepen.",
        explanation = "Voor deze datum is in AGAPE nog geen specifieke gedenkdag opgenomen. Je kunt de dag beginnen met een gebed en de voorgestelde Bijbelgedeelten.",
        rank = "ordinary",
        titleEn = "A day for prayer and reflection",
        teaserEn = "The Church commemorates the saints who fell asleep on this day.",
        explanationEn = "AGAPE does not yet include a specific commemoration for this date. Begin the day with a prayer and the suggested Bible passages."
    )

    private fun label(civilDate: Long, rite: CalendarRite, locale: AppLocale): String {
        val lit = liturgicalMonthDay(civilDate, rite)
        val months = if (locale == AppLocale.en) {
            listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        } else {
            listOf("januari", "februari", "maart", "april", "mei", "juni", "juli", "augustus", "september", "oktober", "november", "december")
        }
        val name = months[lit.first - 1]
        return if (rite == CalendarRite.julian) {
            if (locale == AppLocale.en) "${lit.second} $name according to the old calendar"
            else "${lit.second} $name volgens de oude kalender"
        } else "${lit.second} $name"
    }

    private data class Movable(
        val title: String, val teaser: String, val explanation: String,
        val tone: String, val season: String, val rank: String
    )

    private fun movableOn(date: Long, locale: AppLocale): Movable? {
        val year = cal(date).get(Calendar.YEAR)
        val pascha = orthodoxPascha(year)
        val prev = orthodoxPascha(year - 1)
        fun same(a: Long, b: Long) = ymd(a) == ymd(b)
        val en = locale == AppLocale.en
        fun items(p: Long) = listOf(
            addDays(p, -48) to Movable(
                if (en) "Clean Monday" else "Schone maandag",
                if (en) "Great Lent begins. Prayer of Ephrem, sobriety, alms." else "De Grote Vasten begint. Gebed van Efrem, nuchterheid, aalmoes.",
                if (en) "The gate of the Fast. We ask forgiveness, set the pots aside, and take up the prayer of Ephrem." else "De poort van de vasten. Wij vragen vergeving, leggen de vleespotten weg, en nemen het gebed van Efrem ter hand.",
                "fast", if (en) "Great Lent" else "Grote Vasten", "major"
            ),
            addDays(p, -7) to Movable(
                if (en) "Palm Sunday" else "Palmzondag",
                if (en) "The King comes meekly, on a donkey, to His voluntary Passion." else "De Koning komt zachtmoedig, op een ezel, naar Zijn vrijwillig lijden.",
                if (en) "Lazarus is raised. The palms and the children." else "Lazarus is opgewekt. De palmen en de kinderen.",
                "feast", if (en) "Holy Week" else "Goede Week", "great"
            ),
            addDays(p, -3) to Movable(
                if (en) "Holy Thursday" else "Grote donderdag",
                if (en) "The supper, the washing of feet, the prayer in Gethsemane." else "Het avondmaal, de voetwassing, het gebed in Gethsémané.",
                if (en) "He washes the feet. He breaks the bread." else "Hij wast de voeten. Hij breekt het brood.",
                "fast", if (en) "Holy Week" else "Goede Week", "great"
            ),
            addDays(p, -2) to Movable(
                if (en) "Holy Friday" else "Grote vrijdag",
                if (en) "The Cross. The shroud. The Theotokos at the foot." else "Het kruis. De grafdoek. De Theotokos aan de voet.",
                if (en) "He who sanctified the Jordan hangs upon the wood." else "Hij die de Jordaan heiligde, hangt aan het hout.",
                "fast", if (en) "Holy Week" else "Goede Week", "great"
            ),
            addDays(p, -1) to Movable(
                if (en) "Holy Saturday" else "Grote zaterdag",
                if (en) "The body in the tomb, the soul in Hades. The light waits." else "Het lichaam in het graf, de ziel in de hades. Het licht wacht.",
                if (en) "He rests on the Sabbath." else "Hij rust de sabbat.",
                "fast", if (en) "Holy Week" else "Goede Week", "great"
            ),
            p to Movable(
                if (en) "Pascha — Christ is risen" else "Pasen — Christus is opgestaan",
                if (en) "Death is overthrown. Christ is risen indeed." else "De dood is overwonnen. Christus is waarlijk opgestaan.",
                if (en) "The feast of feasts. The stone is rolled away." else "Het feest der feesten. De steen is weggerold.",
                "pascha", if (en) "Pascha" else "Pasen", "great"
            ),
            addDays(p, 39) to Movable(
                if (en) "Ascension" else "Hemelvaart",
                if (en) "Human nature is carried into heaven." else "De menselijke natuur wordt ten hemel gedragen.",
                if (en) "Forty days after Pascha." else "Veertig dagen na Pasen.",
                "feast", if (en) "Pascha" else "Pasen", "great"
            ),
            addDays(p, 49) to Movable(
                if (en) "Pentecost" else "Pinksteren",
                if (en) "The Spirit descends. The Church is brought to life." else "De Geest daalt neder. De Kerk wordt tot leven gewekt.",
                if (en) "Fifty days. Tongues as of fire." else "Vijftig dagen. Tongen als van vuur.",
                "feast", if (en) "Pentecost" else "Pinksteren", "great"
            ),
            addDays(p, 56) to Movable(
                if (en) "All Saints" else "Allerheiligen",
                if (en) "The Sunday after Pentecost." else "De zondag na Pinksteren.",
                if (en) "The Spirit has filled the Church." else "De Geest heeft de Kerk vervuld.",
                "feast", if (en) "Pentecost" else "Pinksteren", "major"
            )
        )
        val candidates = items(prev) + items(pascha)
        candidates.firstOrNull { same(it.first, date) }?.let { return it.second }
        val toPascha = daysBetween(date, pascha)
        if (toPascha in 1..6) {
            return Movable(
                if (en) "Holy Week" else "Goede Week",
                if (en) "From the alabaster jar to the tomb." else "Van het zalfvat tot het graf.",
                if (en) "The Bridegroom services until the tomb." else "De Bruidegom-diensten tot het graf.",
                "fast", if (en) "Holy Week" else "Goede Week", "major"
            )
        }
        if (toPascha < 0 && toPascha > -7) {
            return Movable(
                if (en) "Bright Week" else "Lichte week",
                if (en) "The doors stand open. There is no fasting." else "De deuren open, geen vasten.",
                if (en) "The whole week is Pascha." else "Heel de week is Pasen.",
                "pascha", if (en) "Pascha" else "Pasen", "major"
            )
        }
        return null
    }

    private data class Season(val tone: String, val season: String, val teaser: String?)

    private fun seasonAround(date: Long, rite: CalendarRite, locale: AppLocale): Season {
        val year = cal(date).get(Calendar.YEAR)
        val pascha = orthodoxPascha(year)
        val lentStart = addDays(pascha, -48)
        val pentecost = addDays(pascha, 49)
        if (date >= lentStart && date < pascha) return Season("fast", seasonLabel("Grote Vasten", locale), null)
        if (date > pascha && date < pentecost) return Season("pascha", seasonLabel("Pasen", locale), null)
        val dormStart = civilDateForMenologion(year, 8, 1, rite)
        val dormEnd = civilDateForMenologion(year, 8, 15, rite)
        if (date >= dormStart && date < dormEnd) {
            return Season("fast", seasonLabel("Vasten van de Theotokos", locale), if (locale == AppLocale.en) "Two weeks toward her Dormition." else "Twee weken naar haar ontslapen.")
        }
        val natThis = civilDateForMenologion(year, 12, 25, rite)
        val fastThis = civilDateForMenologion(year, 11, 15, rite)
        val fastPrevious = civilDateForMenologion(year - 1, 11, 15, rite)
        val natPrevious = civilDateForMenologion(year - 1, 12, 25, rite)
        if ((date >= fastThis && date < natThis) || (date >= fastPrevious && date < natPrevious)) {
            return Season("fast", seasonLabel("Geboortevasten", locale), if (locale == AppLocale.en) "Forty days toward the manger." else "Veertig dagen naar de kribbe.")
        }
        val peter = civilDateForMenologion(year, 6, 29, rite)
        val apostlesStart = addDays(pentecost, 8)
        if (date >= apostlesStart && date < peter) {
            return Season("fast", seasonLabel("Apostelenvasten", locale), if (locale == AppLocale.en) "From Pentecost to Peter and Paul." else "Van Pinksteren tot Petrus en Paulus.")
        }
        return Season("ordinary", seasonLabel("Tijd van het jaar", locale), null)
    }

    data class JourneyGate(val open: Boolean, val opens: String)

    fun journeyGate(id: String, rite: CalendarRite, locale: AppLocale, on: Long = System.currentTimeMillis()): JourneyGate? {
        val today = startOfDay(on)
        val window = when (id) {
            "reis-pasen" -> paschaWindow(today)
            "reis-advent" -> adventWindow(today, rite)
            "reis-kerst" -> christmasWindow(today, rite)
            else -> return null
        }
        return JourneyGate(today >= window.first && today < window.second, dayMonth(window.first, locale))
    }

    private fun paschaWindow(today: Long): Pair<Long, Long> {
        val year = cal(today).get(Calendar.YEAR)
        var pascha = orthodoxPascha(year)
        var start = addDays(pascha, -48)
        var end = addDays(pascha, 50)
        if (today >= end) {
            pascha = orthodoxPascha(year + 1)
            start = addDays(pascha, -48)
            end = addDays(pascha, 50)
        }
        return start to end
    }

    private fun adventWindow(today: Long, rite: CalendarRite): Pair<Long, Long> {
        val year = cal(today).get(Calendar.YEAR)
        val priorStart = civilDateForMenologion(year - 1, 11, 15, rite)
        val priorEnd = civilDateForMenologion(year - 1, 12, 25, rite)
        if (today >= priorStart && today < priorEnd) return priorStart to priorEnd
        var start = civilDateForMenologion(year, 11, 15, rite)
        var end = civilDateForMenologion(year, 12, 25, rite)
        if (today >= end) {
            start = civilDateForMenologion(year + 1, 11, 15, rite)
            end = civilDateForMenologion(year + 1, 12, 25, rite)
        }
        return start to end
    }

    private fun christmasWindow(today: Long, rite: CalendarRite): Pair<Long, Long> {
        val year = cal(today).get(Calendar.YEAR)
        val previous = civilDateForMenologion(year - 1, 12, 25, rite)
        if (today < addDays(previous, 12)) return previous to addDays(previous, 12)
        val start = civilDateForMenologion(year, 12, 25, rite)
        return start to addDays(start, 12)
    }

    private fun startOfDay(millis: Long): Long {
        val c = cal(millis)
        return civil(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    private fun dayMonth(millis: Long, locale: AppLocale): String {
        val fmt = java.text.SimpleDateFormat("d MMMM", if (locale == AppLocale.en) java.util.Locale.ENGLISH else java.util.Locale("nl"))
        return fmt.format(java.util.Date(millis))
    }

    private fun cal(millis: Long): Calendar {
        val c = GregorianCalendar(TimeZone.getDefault())
        c.timeInMillis = millis
        return c
    }
}
