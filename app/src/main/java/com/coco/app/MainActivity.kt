package com.coco.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.app.ui.home.HomeScreen
import com.coco.app.ui.home.HomeViewModel
import com.coco.app.ui.theme.CocoTheme
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    private var sharedViewModel: HomeViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val composeReady = AtomicBoolean(false)
        installSplashScreen().setKeepOnScreenCondition { !composeReady.get() }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
        )

        val app = application as CocoApplication

        setContent {
            CocoTheme {
                val viewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.factory(app.noteRepository, app.settingsStore),
                )
                sharedViewModel = viewModel
                LaunchedEffect(Unit) {
                    handleIntent(intent)
                }
                HomeScreen(
                    viewModel = viewModel,
                    onComposeReady = { composeReady.set(true) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            sharedViewModel?.onSharedTextReceived(text)
            intent.removeExtra(Intent.EXTRA_TEXT)
        } else if (intent?.getBooleanExtra("ACTION_QUICK_CAPTURE", false) == true) {
            sharedViewModel?.triggerQuickCapture()
            intent.removeExtra("ACTION_QUICK_CAPTURE")
        }
    }
}
