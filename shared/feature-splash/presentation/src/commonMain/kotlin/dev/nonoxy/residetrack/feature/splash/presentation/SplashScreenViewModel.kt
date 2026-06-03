package dev.nonoxy.residetrack.feature.splash.presentation

import androidx.lifecycle.viewModelScope
import dev.nonoxy.residetrack.core.initializer.AppInitializer
import dev.nonoxy.residetrack.core.presentation.viewmodel.BaseViewModel
import dev.nonoxy.residetrack.feature.splash.presentation.models.UiSplashLabel
import kotlinx.coroutines.launch

class SplashScreenViewModel internal constructor(
    private val appInitializer: AppInitializer,
) : BaseViewModel<Unit, UiSplashLabel>(initialState = Unit) {

    init {
        viewModelScope.launch {
            appInitializer.run()
            acceptLabel(UiSplashLabel.NavigateToRooms)
        }
    }
}
