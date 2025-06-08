package dev.nonoxy.feature.rooms.presentation.models

import dev.nonoxy.feature.rooms.ui.models.UiRoom
import dev.nonoxy.feature.rooms.ui.models.UiStudent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap

internal data class RoomsViewState(
    val tabsState: RoomsTabsState,
    val allRooms: ImmutableList<UiRoom>,
    val roomsOnFloor: ImmutableMap<Int, ImmutableList<UiRoom>>,
    val selectedFloorTotalBeds: Int,
    val selectedFloorAvailableBeds: Int
) {
    companion object {
        val Initial: RoomsViewState = RoomsViewState(
            tabsState = RoomsTabsState.Initial,
            allRooms = persistentListOf(),
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
                5 to persistentListOf(
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
                6 to persistentListOf(
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
                6 to persistentListOf(
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
                7 to persistentListOf(
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
                8 to persistentListOf(
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
                9 to persistentListOf(
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
                10 to persistentListOf(
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
                11 to persistentListOf(
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
                12 to persistentListOf(
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
                )
            ).filter { it.key < 7 }.toPersistentMap(),
            selectedFloorTotalBeds = 0,
            selectedFloorAvailableBeds = 0
        )
    }
}

internal data class RoomsTabsState(
    val selectedTabIndex: Int,
    val selectedFloor: Int?,
) {
    companion object {
        val Initial: RoomsTabsState = RoomsTabsState(
            selectedTabIndex = 0,
            selectedFloor = null,
        )
    }
}
