package agape.app.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object Haptics {
    var isEnabled = true

    fun light(context: Context? = null) {
        if (!isEnabled) return
        vibrate(context, 12)
    }

    fun success(context: Context? = null) {
        if (!isEnabled) return
        vibrate(context, 24)
    }

    fun select(context: Context? = null) {
        if (!isEnabled) return
        vibrate(context, 8)
    }

    private fun vibrate(context: Context?, ms: Long) {
        val ctx = context ?: return
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        }
    }

    fun perform(view: View, type: Int = HapticFeedbackConstants.KEYBOARD_TAP) {
        if (!isEnabled) return
        view.performHapticFeedback(type)
    }
}
