package agape.app.data

object PrayerPoints {
    fun point(id: String) = all.firstOrNull { it.id == id }
    fun filtered(group: PrayerPointGroup?) = if (group == null) all else all.filter { it.group == group }
    fun ofDay(dateMillis: Long = System.currentTimeMillis()): PrayerPoint {
        val day = Readings.dayOfYear(dateMillis)
        return all[day % all.size]
    }

    val all: List<PrayerPoint> = listOf(
        p("dankbaarheid", PrayerPointGroup.personal, "Dankbaarheid", "Thanksgiving",
            "Heer, ik dank U voor deze dag en voor elk goed dat van U komt. Leer mij te zien wat Gij geeft.",
            "Lord, I thank You for this day and for every good gift from Your hand. Teach me to see what You give.",
            listOf(r("psalmen", 103), r("1thessalonicenzen", 5, 18))),
        p("vergeving", PrayerPointGroup.personal, "Vergeving", "Forgiveness",
            "Heere, vergeef mij mijn zonden. Schep in mij een rein hart en vernieuw in het binnenste van mij een vaste geest.",
            "Lord, forgive my sins. Create in me a clean heart and renew a right spirit within me.",
            listOf(r("psalmen", 51), r("1johannes", 1, 9))),
        p("wijsheid", PrayerPointGroup.personal, "Wijsheid", "Wisdom",
            "Heere, geef mij wijsheid van boven: rein, vreedzaam en vol ontferming. Leer mij Uw wil te verstaan.",
            "Lord, give me wisdom from above: pure, peaceable and full of mercy. Teach me to know Your will.",
            listOf(r("spreuken", 3), r("jakobus", 1, 5))),
        p("kracht", PrayerPointGroup.personal, "Kracht", "Strength",
            "Heere, wees mijn sterkte. Wanneer ik zwak ben, wees Gij krachtig in mij.",
            "Lord, be my strength. When I am weak, be powerful in me.",
            listOf(r("psalmen", 27), r("2korintiers", 12, 9))),
        p("geloof", PrayerPointGroup.personal, "Geloof", "Faith",
            "Heere, ik geloof; kom mijn ongeloof te hulp. Bewaar mijn vertrouwen op U.",
            "Lord, I believe; help my unbelief. Keep my trust in You.",
            listOf(r("marcus", 9, 24), r("hebreen", 11))),
        p("familie", PrayerPointGroup.people, "Voor familie", "For family",
            "Heer, waak over mijn familie. Geef hen kracht, vrede en wijsheid.",
            "Lord, watch over my family. Give them strength, peace and wisdom.",
            listOf(r("psalmen", 23), r("filippenzen", 4, 6))),
        p("vrienden", PrayerPointGroup.people, "Voor vrienden", "For friends",
            "Heere, zegen hen die Gij naast mij gesteld hebt. Bewaar onze vriendschap in waarheid en liefde.",
            "Lord, bless those You have set beside me. Keep our friendship in truth and love.",
            listOf(r("johannes", 15), r("spreuken", 17, 17))),
        p("relaties", PrayerPointGroup.people, "Voor relaties", "For relationships",
            "Heere, leer mij lief te hebben zoals Gij liefhebt: geduldig, ootmoedig, zonder wrok.",
            "Lord, teach me to love as You love: patient, humble, without resentment.",
            listOf(r("1korintiers", 13), r("efeziers", 4, 2))),
        p("zieken", PrayerPointGroup.people, "Voor wie ziek zijn", "For the sick",
            "Heere, zie op hen die lijden. Geef verlichting, geduld en hoop, en wees nabij in hun nacht.",
            "Lord, look upon those who suffer. Give relief, patience and hope, and be near in their night.",
            listOf(r("psalmen", 41), r("jakobus", 5, 14))),
        p("werk", PrayerPointGroup.daily, "Voor werk", "For work",
            "Heere, zegen het werk van mijn handen. Bewaar mij voor trots en voor angst, en laat mijn arbeid tot nut zijn.",
            "Lord, bless the work of my hands. Keep me from pride and from fear, and let my labor be of use.",
            listOf(r("kolossenzen", 3, 23), r("psalmen", 90))),
        p("studie", PrayerPointGroup.daily, "Voor studie", "For study",
            "Heere, verlicht mijn verstand. Geef aandacht, nederigheid en volharding in wat ik leer.",
            "Lord, enlighten my mind. Give attention, humility and endurance in what I learn.",
            listOf(r("spreuken", 2), r("psalmen", 119))),
        p("beslissingen", PrayerPointGroup.daily, "Voor beslissingen", "For decisions",
            "Heere, leid mijn stappen. Waar ik niet weet te kiezen, schenk mij Uw vrede als wachter over mijn hart.",
            "Lord, lead my steps. Where I do not know how to choose, give Your peace to guard my heart.",
            listOf(r("spreuken", 3, 5), r("filippenzen", 4, 7))),
        p("toekomst", PrayerPointGroup.daily, "Voor de toekomst", "For the future",
            "Heere, mijn tijden zijn in Uw hand. Bewaar mij voor zorg over morgen en leer mij heden te wandelen.",
            "Lord, my times are in Your hand. Keep me from anxiety about tomorrow and teach me to walk today.",
            listOf(r("matteus", 6), r("psalmen", 31))),
        p("kerk", PrayerPointGroup.world, "Voor de Kerk", "For the Church",
            "Heere, bewaar Uw Kerk in eenheid, waarheid en liefde. Geef herders naar Uw hart.",
            "Lord, keep Your Church in unity, truth and love. Give shepherds after Your own heart.",
            listOf(r("efeziers", 4), r("johannes", 17))),
        p("vrede", PrayerPointGroup.world, "Voor vrede", "For peace",
            "Koning des vredes, schenk vrede waar haat heerst, en leer ons vredestichters te zijn.",
            "King of peace, grant peace where hatred rules, and teach us to be peacemakers.",
            listOf(r("psalmen", 46), r("matteus", 5))),
        p("armen", PrayerPointGroup.world, "Voor de armen", "For the poor",
            "Heere, gedenk de armen en de verlatenen. Open mijn handen, dat ik niet voorbijga.",
            "Lord, remember the poor and the forsaken. Open my hands, that I may not pass by.",
            listOf(r("spreuken", 19, 17), r("matteus", 25))),
        p("vervolging", PrayerPointGroup.world, "Voor vervolgde christenen", "For the persecuted",
            "Heere, versterk hen die om Uw Naam lijden. Geef hun standvastigheid en ons een wakker hart.",
            "Lord, strengthen those who suffer for Your Name. Give them steadfastness and give us a watchful heart.",
            listOf(r("matteus", 5, 10), r("hebreen", 13, 3))),
        p("leiders", PrayerPointGroup.world, "Voor leiders", "For leaders",
            "Heere, geef hen die gezag dragen wijsheid en recht. Bewaar ons land in gerechtigheid.",
            "Lord, give those in authority wisdom and justice. Keep our land in righteousness.",
            listOf(r("1timotheus", 2), r("psalmen", 72))),
        p("wereldnood", PrayerPointGroup.world, "Voor nood in de wereld", "For the world’s need",
            "Heere der heirscharen, zie op oorlog, honger en ontworteling. Ontferm U, en maak ons tot naasten.",
            "Lord of hosts, look upon war, hunger and displacement. Have mercy, and make us neighbors.",
            listOf(r("psalmen", 10), r("lucas", 10)))
    )

    private fun p(id: String, group: PrayerPointGroup, nl: String, en: String, bodyNl: String, bodyEn: String, refs: List<ChapterRef>) =
        PrayerPoint(id, group, nl, en, bodyNl, bodyEn, refs)

    private fun r(bookId: String, chapter: Int, verse: Int? = null) = ChapterRef(bookId, chapter, verse)
}

object GuidedPrayer {
    val audiences = listOf(
        PrayerAudience(
            "self", "Voor mijzelf", "For myself", false,
            "Je hoeft geen kerktaal te kennen. Zeg wat waar is, rustig, alsof je tot iemand spreekt die je hoort.",
            "You do not need church language. Say what is true, quietly, as if speaking to someone who hears you.",
            listOf("Ga even stil zitten. Adem twee keer langzaam.", "Begin eenvoudig: ‘God, ik ben hier.’ Dat is al genoeg.", "Zeg wat je nodig hebt: rust, vergeving, kracht, of alleen nabijheid.", "Eindig niet met een prestatie. Eindig met Amen — het betekent: laat het zo zijn."),
            listOf("Sit still for a moment. Breathe slowly twice.", "Begin simply: ‘God, I am here.’ That is already enough.", "Say what you need: rest, forgiveness, strength, or simply nearness.", "Do not end with a performance. End with Amen — it means: let it be so."),
            "God, ik weet niet altijd hoe ik tot U moet spreken. Ontvang mij zoals ik ben. Geef mij rust, en leer mij te vertrouwen.",
            "God, I do not always know how to speak to You. Receive me as I am. Give me rest, and teach me to trust.",
            "geloof"
        ),
        PrayerAudience(
            "loved", "Voor iemand van wie ik houd", "For someone I love", true,
            "Noem de naam zacht. Je bidt niet om controle. Je legt die persoon in grotere handen.",
            "Say the name quietly. You are not praying to control. You are placing that person in greater hands.",
            listOf("Zeg eerst de naam. Dan weet je hart voor wie je staat.", "Vraag niet alles tegelijk. Kies één ding: vrede, wijsheid, of bescherming.", "Als je niet gelooft, mag je toch zeggen: ‘Als U er bent, waak over hen.’", "Houd het kort. Liefde hoeft niet lang te zijn om waar te zijn."),
            listOf("Say the name first. Then your heart knows for whom you stand.", "Do not ask everything at once. Choose one thing: peace, wisdom, or protection.", "If you do not believe, you may still say: ‘If You are there, watch over them.’", "Keep it short. Love does not have to be long to be true."),
            "Heer, ik leg {naam} bij U. Waak over hen. Geef kracht, vrede en wijsheid, meer dan ik kan geven.",
            "Lord, I place {name} with You. Watch over them. Give strength, peace and wisdom, more than I can give.",
            "familie"
        ),
        PrayerAudience(
            "sick", "Voor iemand die ziek is", "For someone who is sick", true,
            "Bij ziekte bid je niet als arts. Je blijft nabij, en je vraagt ontferming.",
            "In sickness you do not pray as a doctor. You stay near, and you ask for mercy.",
            listOf("Begin met de naam, niet met de ziekte.", "Vraag verlichting, geduld en hoop — ook als genezing onzeker is.", "Je mag zwijgen na de woorden. Stilte is ook gebed.", "Eindig met ontferming, niet met beloften die je niet kunt houden."),
            listOf("Begin with the name, not the illness.", "Ask for relief, patience and hope — even if healing is uncertain.", "You may be silent after the words. Silence is also prayer.", "End with mercy, not with promises you cannot keep."),
            "Heer, zie {naam}. Wees nabij in pijn en angst. Geef verlichting, en houd hen vast.",
            "Lord, look upon {name}. Be near in pain and fear. Give relief, and hold them fast.",
            "zieken"
        ),
        PrayerAudience(
            "work", "Voor werk of een besluit", "For work or a decision", false,
            "Je hoeft het pad niet te kennen. Vraag licht voor de volgende stap.",
            "You do not need to know the path. Ask for light for the next step.",
            listOf("Zeg wat er speelt, in gewone woorden.", "Vraag wijsheid, niet alleen succes.", "Laat ruimte: ‘Uw wil, niet alleen de mijne.’", "Als je niet bidt tot Christus, mag je tot God of tot het Goede spreken. Begin waar je bent."),
            listOf("Say what is at stake, in ordinary words.", "Ask for wisdom, not only success.", "Leave room: ‘Your will, not only mine.’", "If you do not pray to Christ, you may speak to God or to the Good. Begin where you are."),
            "God, ik weet niet altijd wat het rechte is. Verlicht mijn verstand. Bewaar mij voor angst en voor trots, en leid mijn stappen.",
            "God, I do not always know what is right. Enlighten my mind. Keep me from fear and from pride, and lead my steps.",
            "beslissingen"
        ),
        PrayerAudience(
            "world", "Voor de wereld", "For the world", false,
            "Je draagt niet alles. Je noemt één nood, en je laat die niet alleen.",
            "You do not carry everything. You name one need, and you do not leave it alone.",
            listOf("Kies één plek of één groep. Anders wordt het te groot.", "Bid om vrede en om ontferming, niet om gelijk te hebben.", "Vraag ook wat jij zelf kunt doen, klein als het is.", "Eindig stil. De wereld is groter dan jouw woorden."),
            listOf("Choose one place or one group. Otherwise it becomes too large.", "Pray for peace and for mercy, not to be proved right.", "Also ask what you yourself can do, however small.", "End in silence. The world is larger than your words."),
            "Heer der wereld, zie op oorlog, honger en eenzaamheid. Ontferm U. Maak ons tot naasten, ook waar wij veraf staan.",
            "Lord of the world, look upon war, hunger and loneliness. Have mercy. Make us neighbors, even where we stand far off.",
            "wereldnood"
        ),
        PrayerAudience(
            "unsure", "Ik weet het nog niet", "I am not sure yet", false,
            "Dan begin je met aanwezig zijn. Dat is het eerste gebed.",
            "Then you begin by being present. That is the first prayer.",
            listOf("Je hoeft niets te presteren. Zitten is al een begin.", "Zeg: ‘Ik ben hier.’ Wacht even. Kijk wat opkomt.", "Als er niets komt, is dat geen mislukking.", "Het Jezusgebed is kort en oud: Heer Jezus Christus, ontferm U over mij."),
            listOf("You do not have to perform. Sitting is already a beginning.", "Say: ‘I am here.’ Wait a moment. See what arises.", "If nothing comes, that is not a failure.", "The Jesus Prayer is short and old: Lord Jesus Christ, have mercy on me."),
            "God, ik weet niet wat ik vragen moet. Ontvang mijn stilte. Wees nabij.",
            "God, I do not know what to ask. Receive my silence. Be near.",
            "dankbaarheid"
        )
    )
}
