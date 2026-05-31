package dev.nonoxy.residetrack.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.navigation.ResideTrackNavHost

@Composable
fun App() {
    ResideTrackTheme {
        ResideTrackNavHost(modifier = Modifier.fillMaxSize())
    }
}
