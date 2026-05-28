package dev.nonoxy.residetrack

import androidx.compose.ui.window.ComposeUIViewController
import dev.nonoxy.residetrack.di.initKoin
import dev.nonoxy.residetrack.ui.App
import platform.UIKit.UIViewController

private var koinStarted = false

fun MainViewController(): UIViewController {
    if (!koinStarted) {
        koinStarted = true
        initKoin()
    }
    return ComposeUIViewController { App() }
}
