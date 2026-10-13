package agape.app.ui.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import agape.app.store.LocalStore
import agape.app.theme.*
import agape.app.ui.components.*

@Composable
fun PrayerCounter(sessionId: String, showInvocation: Boolean = true) {
    val store = LocalStore.current
    val count = (store.state.prayerSessions[sessionId] ?: 0).coerceIn(0, 41)
    val complete = count == 41
    var confirmReset by rememberSaveable(sessionId) { mutableStateOf(false) }
    val progressLabel = "$count / 41"
    val tapLabel = store.t("counterTap")
    AgapeCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Kicker(store.t("counterTitle"), AppColor.accent())
            if (showInvocation) Text(store.t("counterInvocation"), style = AppType.serif(23.sp),
                color = AppColor.fg(), textAlign = TextAlign.Center, lineHeight = 31.sp)
            Text(store.t(if (complete) "counterComplete" else "counterHint"),
                style = AppType.sans(14.sp), color = AppColor.muted(), textAlign = TextAlign.Center)
            Box(
                Modifier.size(208.dp).clip(CircleShape).background(AppColor.accent().copy(alpha = 0.07f))
                    .clickable(enabled = !complete, role = Role.Button, onClickLabel = tapLabel) {
                        store.advancePrayerSession(sessionId)
                    }
                    .semantics(mergeDescendants = true) {
                        contentDescription = tapLabel
                        stateDescription = progressLabel
                    },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(progress = { count / 41f }, modifier = Modifier.fillMaxSize().padding(7.dp),
                    color = AppColor.accent(), trackColor = AppColor.line(), strokeWidth = 5.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$count", style = AppType.display(64.sp), color = AppColor.accent())
                    Text(store.t("counterOf41"), style = AppType.sans(14.sp), color = AppColor.muted())
                    Spacer(Modifier.height(8.dp))
                    Text(store.t(if (complete) "prayedAmen" else "counterTap"),
                        style = AppType.sans(13.sp), color = AppColor.fg())
                }
            }
            TextButton(onClick = { if (complete) store.resetPrayerSession(sessionId) else confirmReset = true },
                enabled = count > 0) {
                Text(store.t(if (complete) "counterNew" else "counterReset"), color = if (count > 0) AppColor.accent() else AppColor.faint())
            }
        }
    }
    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text(store.t("counterResetTitle")) },
        text = { Text(store.t("counterResetBody")) },
        confirmButton = { TextButton(onClick = { store.resetPrayerSession(sessionId); confirmReset = false }) { Text(store.t("counterReset")) } },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(store.t("cancel")) } }
    )
}
