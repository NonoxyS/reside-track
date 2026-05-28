package dev.nonoxy.residetrack.feature.rooms.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.common.utils.orEmptyPersist
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiRoom
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiRoomsState
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiStudent
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomsScreenDetails(
    state: UiRoomsState,
    onRoomClick: (Long) -> Unit,
    onAddRoomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { state.roomsOnFloor.keys.size }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        RoomsTopBar(
            modifier = Modifier.padding(horizontal = padding_size_16),
            totalPlaces = 0,
            availablePlaces = 0,
            onAddRoomClick = onAddRoomClick
        )

        if (state.roomsOnFloor.keys.size > 1) {
            RoomsTabRow(
                modifier = Modifier.fillMaxWidth(),
                floors = state.roomsOnFloor.keys,
                selectedTabIndex = pagerState.currentPage,
                onTabClick = { index ->
                    scope.launch { pagerState.animateScrollToPage(page = index) }
                }
            )
        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { page ->
            RoomList(
                rooms = state.roomsOnFloor.get(
                    key = state.roomsOnFloor.keys.elementAtOrNull(index = page)
                ).orEmptyPersist(),
                onRoomClick = onRoomClick
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        RoomsScreenDetails(
            state = UiRoomsState(
                roomsOnFloor = persistentMapOf(
                    3 to persistentListOf(
                        UiRoom(
                            id = 1,
                            floorNumber = "3",
                            roomNumber = "329",
                            bedsCount = "5",
                            students = persistentListOf(
                                UiStudent(
                                    streamNumber = "1234",
                                    checkInDate = "12-03-2024",
                                    checkOutDate = "31-03-2024",
                                    isCheckOutDateNearOrExpired = false
                                ),
                                UiStudent(
                                    streamNumber = "5646",
                                    checkInDate = "12-03-2024",
                                    checkOutDate = "15-03-2024",
                                    isCheckOutDateNearOrExpired = true
                                ),
                            )
                        ),
                    ),
                    4 to persistentListOf(
                        UiRoom(
                            id = 2,
                            floorNumber = "4",
                            roomNumber = "401",
                            bedsCount = "3",
                            students = persistentListOf()
                        ),
                    )
                )
            ),
            onRoomClick = {},
            onAddRoomClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
