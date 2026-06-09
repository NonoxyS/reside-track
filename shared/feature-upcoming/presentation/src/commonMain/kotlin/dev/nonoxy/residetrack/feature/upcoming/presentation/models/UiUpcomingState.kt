package dev.nonoxy.residetrack.feature.upcoming.presentation.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UiUpcomingState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val items: ImmutableList<UiUpcomingItem> = persistentListOf(),
)
