package agape.app.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import agape.app.R
import agape.app.store.LocalStore
import agape.app.theme.*
import agape.app.ui.components.*

@Composable
fun WelcomeView() {
    val store = LocalStore.current
    var chosen by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf(store.state.profileName) }
    Column(Modifier.fillMaxSize().background(AppColor.bg()).safeDrawingPadding()
        .imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Image(painterResource(R.drawable.agape_icon), null, Modifier.size(104.dp).clip(AppRadius.extraLargeShape()))
        Text("AGAPE", style = AppType.display(34.sp).copy(letterSpacing = 5.sp), color = AppColor.accent())
        if (!chosen) {
            Text("Welkom · Welcome", style = AppType.serif(24.sp), color = AppColor.fg())
            Text("Kies je taal / Choose your language", style = AppType.sans(15.sp), color = AppColor.muted())
            AgapeButton("Nederlands") { store.applyLocale("nl"); chosen = true }
            AgapeButton("English", AgapeButtonKind.outline) { store.applyLocale("en"); chosen = true }
        } else {
            Text(store.t("introLead"), style = AppType.serif(25.sp), color = AppColor.fg(), textAlign = TextAlign.Center, lineHeight = 34.sp)
            WelcomeInformation()
            OutlinedTextField(value = name, onValueChange = { name = it.take(60) }, singleLine = true,
                label = { Text(store.t("profileName")) }, supportingText = { Text(store.t("profileNameHint")) },
                modifier = Modifier.fillMaxWidth(), shape = AppRadius.mediumShape())
            Button(onClick = { store.completeWelcome(name) }, modifier = Modifier.fillMaxWidth(), shape = AppRadius.mediumShape()) {
                Text(store.t("profileStart"), modifier = Modifier.padding(vertical = 8.dp))
            }
            Text(store.t("profilePrivacy"), style = AppType.sans(14.sp), color = AppColor.muted(), textAlign = TextAlign.Center, lineHeight = 21.sp)
            TextButton(onClick = { chosen = false }) { Text(store.t("chooseLanguage")) }
        }
    }
}

@Composable
private fun WelcomeInformation() {
    val store = LocalStore.current
    AgapeCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Kicker(store.t("appInformation"))
            listOf("introBible", "introPrayer", "introCalendar").forEach { key ->
                Text(store.t(key), style = AppType.sans(16.sp), color = AppColor.fg(), lineHeight = 24.sp)
            }
        }
    }
}

@Composable
fun ProfileView() {
    val store = LocalStore.current
    var name by rememberSaveable { mutableStateOf(store.state.profileName) }
    var saved by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(AppColor.bg()).imePadding().verticalScroll(rememberScrollState())) {
        AppScreenColumn {
            ScreenHeader("AGAPE", store.t("localProfile"), detail = store.t("profilePrivacy"))
            OutlinedTextField(value = name, onValueChange = { name = it.take(60); saved = false }, singleLine = true,
                label = { Text(store.t("profileName")) }, supportingText = { Text(store.t("profileNameHint")) },
                modifier = Modifier.fillMaxWidth(), shape = AppRadius.mediumShape())
            AgapeButton(store.t(if (saved) "profileSaved" else "profileSave")) { store.saveProfileName(name); saved = true }
            AgapeCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Kicker(store.t("profileStorageTitle"))
                    Text(store.t("profileStorageBody"), style = AppType.sans(16.sp), color = AppColor.fg(), lineHeight = 24.sp)
                    Text(store.t("profileNoSync"), style = AppType.sans(15.sp), color = AppColor.muted(), lineHeight = 23.sp)
                }
            }
            WelcomeInformation()
        }
    }
}
