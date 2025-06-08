package dev.nonoxy.residetrack

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.residetrack.di.appModule
import dev.nonoxy.residetrack.navigation.ResideTrackNavHost
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    ResideTrackTheme {
        KoinMultiplatformApplication(
            config = koinConfiguration { appModule() }
        ) {
            ResideTrackNavHost(modifier = Modifier.fillMaxSize())
        }
    }
}
