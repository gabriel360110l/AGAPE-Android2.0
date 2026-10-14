# ☩ AGAPE — Orthodoxe Bijbel, Agpeya & Patristische Traditie (Android)

**AGAPE** is een rustige, sacrale Android-applicatie (Kotlin + Jetpack Compose) voor het lezen van de Heilige Schrift, het bidden van de Koptisch-Orthodoxe getijden (**Agpeya**), en het bestuderen van de **Kerkvaders** en **Zeven Oecumenische Concilies**.

## Belangrijkste Onderdelen & Inhoudelijke Correcties

1. **Bijbellezer met Optionele Vers-Uitleg (†) & Hoofdstuk-Inleidingen:**
   - Gebruikt `bible.sqlite`, `commentary/*.json` en chapter explanations.
   - Verzen met patristisch commentaar tonen optionele uitleg.
   - Met de knop **"Toon/Verberg uitleg"** is het commentaar in- of uit te schakelen.

2. **Liturgisch Correcte *Kyrie Eleison (41×)* in de Agpeya:**
   - Misplaatste vermeldingen van *"Kyrie eleison (41 keer)"* bij losse gebeden zijn verwijderd uit `prayers.json`.
   - In `agpeya-hours.json` staat het 41-voudige *Kyrie Eleison* uitsluitend op het canonieke moment: **na de Korte Gebeden en vóór het Trisagion**, inclusief een rustige teller met haptische feedback.

3. **Full-Screen Intro-Video (ExoPlayer / Media3):**
   - Speelt de intro-video uit `assets/films/` full-screen (`RESIZE_MODE_ZOOM`).
   - Alleen bij de eerste keer openen (`SharedPreferences`).
   - "Overslaan"-knop en automatische overgang na max. 5,5 seconden.
   - Donkere sacrale overlay + branding.

4. **Sacraal Kleuren- en Typografiesysteem:**
   - Kleuren: Parchment, Coptic Burgundy, Byzantine Gold, Vigil dark mode.
   - Integratie van Kerkvaders en Concilies documentatie.

## Bouwen & Uitvoeren

1. Open de map `android/` in Android Studio.
2. Synchroniseer Gradle (JDK 17, minSdk 26).
3. Start de applicatie.

De nieuwe sacrale UI is geïntegreerd op basis van de Gemini-gegenereerde code, aangepast aan het bestaande package (`agape.app`).
