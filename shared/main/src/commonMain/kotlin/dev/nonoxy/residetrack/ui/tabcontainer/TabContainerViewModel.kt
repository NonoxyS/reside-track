package dev.nonoxy.residetrack.ui.tabcontainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nonoxy.residetrack.common.utils.currentLocalDate
import dev.nonoxy.residetrack.core.rooms.repository.RoomsRepository
import dev.nonoxy.residetrack.core.rooms.upcoming.UpcomingCheckouts
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TabContainerViewModel internal constructor(
    roomsRepository: RoomsRepository,
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
}
