package dev.nonoxy.residetrack.ui.tabcontainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nonoxy.residetrack.common.resources.StringConverter
import dev.nonoxy.residetrack.common.utils.currentLocalDate
import dev.nonoxy.residetrack.core.presentation.snackbar.SnackbarBus
import dev.nonoxy.residetrack.core.presentation.snackbar.SnackbarEventType
import dev.nonoxy.residetrack.core.rooms.domain.repository.RoomsRepository
import dev.nonoxy.residetrack.core.rooms.domain.upcoming.UpcomingCheckouts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal data class SnackbarUiEvent(val message: String, val type: SnackbarEventType)

class TabContainerViewModel internal constructor(
    roomsRepository: RoomsRepository,
    snackbarBus: SnackbarBus,
    private val stringConverter: StringConverter,
) : ViewModel() {

    val upcomingCount: StateFlow<Int> =
        roomsRepository.observeRooms()
            .map { rooms ->
                val today = currentLocalDate
                rooms.sumOf { room ->
                    room.students.count { student ->
                        UpcomingCheckouts.isUpcoming(student.checkOutDate, today)
                    }
                }
            }
            .catch { emit(0) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val snackbarEvents: Flow<SnackbarUiEvent> = snackbarBus.events
        .map { event -> SnackbarUiEvent(stringConverter.convert(event.message), event.type) }
}
