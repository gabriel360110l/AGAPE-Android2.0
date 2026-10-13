package agape.app.data

import android.content.Context
import java.io.File

object AssetPack {
    fun unpack(context: Context) {
        copyIfNeeded(context, "bible.sqlite")
        copyIfNeeded(context, "guides.sqlite")
    }

    fun databaseFile(context: Context, name: String): File {
        val dest = context.getDatabasePath(name)
        dest.parentFile?.mkdirs()
        return dest
    }

    private fun copyIfNeeded(context: Context, name: String) {
        val dest = databaseFile(context, name)
        val assetSize = context.assets.open(name).use { it.available().toLong() }
        if (dest.exists() && dest.length() == assetSize) return
        context.assets.open(name).use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
    }
}
