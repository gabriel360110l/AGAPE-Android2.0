package agape.app.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import agape.app.AgapeApplication
import agape.app.R

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val hourId = intent.getStringExtra("hourId") ?: return
        val store = AgapeApplication.instance.store
        val hour = ContentCatalog.hours.firstOrNull { it.id == hourId } ?: return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel("agpeya", "AGAPE", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val builder = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(context, "agpeya")
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
        val n = builder
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(ContentCatalog.hourTitle(hour, store.state.locale))
            .setContentText(store.t("hourSoon"))
            .setAutoCancel(true)
            .build()
        nm.notify(hourId.hashCode(), n)
    }
}
