package agape.app

import android.app.Application
import agape.app.data.AssetPack
import agape.app.data.BibleDatabase
import agape.app.data.ChapterGuideStore
import agape.app.data.ContentCatalog
import agape.app.data.JourneyCatalog
import agape.app.store.AppStore

class AgapeApplication : Application() {
    lateinit var store: AppStore
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        AssetPack.unpack(this)
        ContentCatalog.load(this)
        JourneyCatalog.load(this)
        BibleDatabase.open(this)
        ChapterGuideStore.open(this)
        store = AppStore(this)
    }

    companion object {
        lateinit var instance: AgapeApplication
            private set
    }
}
