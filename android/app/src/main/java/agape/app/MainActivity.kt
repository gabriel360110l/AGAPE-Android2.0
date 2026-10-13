package agape.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import agape.app.store.LocalStore
import agape.app.theme.AgapeRoot
import agape.app.ui.navigation.RootView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val store = AgapeApplication.instance.store
            val night = store.night
            CompositionLocalProvider(LocalStore provides store) {
                AgapeRoot(night = night) {
                    RootView()
                }
            }
        }
    }
}
