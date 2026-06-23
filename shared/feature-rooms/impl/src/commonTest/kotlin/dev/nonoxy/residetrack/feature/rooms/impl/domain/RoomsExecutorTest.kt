package dev.nonoxy.residetrack.feature.rooms.impl.domain

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.nonoxy.residetrack.core.rooms.models.Room
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.residetrack.feature.rooms.impl.data.FakeRoomsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RoomsExecutorTest {

    private fun room(id: Long, floor: Int, number: Int, beds: Int = 4) =
        Room(id = id, floorNumber = floor, roomNumber = number, bedsCount = beds, students = emptyList())

    private fun TestScope.createStore(repository: FakeRoomsRepository): RoomsStore =
        RoomsStoreFactory(
            storeFactory = DefaultStoreFactory(),
            mainDispatcher = UnconfinedTestDispatcher(testScheduler),
            roomsRepository = repository,
        ).create()

    @Test
    fun `LoadInitial groups rooms by floor and clears loading`() = runTest {
        val repository = FakeRoomsRepository(
            rooms = listOf(
                room(id = 1, floor = 2, number = 21),
                room(id = 2, floor = 2, number = 22),
                room(id = 3, floor = 3, number = 31),
            ),
        )

        val store = createStore(repository)

        val state = store.state
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals(setOf(2, 3), state.roomsOnFloor.keys)
        assertEquals(2, state.roomsOnFloor.getValue(2).size)
        assertEquals(1, state.roomsOnFloor.getValue(3).size)

        store.dispose()
    }

    @Test
    fun `observeRooms failure sets error and clears loading`() = runTest {
        val repository = FakeRoomsRepository().apply { observeRoomsError = RuntimeException("boom") }

        val store = createStore(repository)

        assertFalse(store.state.isLoading)
        assertTrue(store.state.isError)

        store.dispose()
    }

    @Test
    fun `OnRetry reloads and recovers from a previous error`() = runTest {
        val repository = FakeRoomsRepository(rooms = listOf(room(id = 1, floor = 2, number = 21)))
            .apply { observeRoomsError = RuntimeException("boom") }
        val store = createStore(repository)
        assertTrue(store.state.isError)

        repository.observeRoomsError = null
        store.accept(Intent.OnRetry)

        assertFalse(store.state.isError)
        assertEquals(setOf(2), store.state.roomsOnFloor.keys)

        store.dispose()
    }

    @Test
    fun `OnRoomClick publishes navigate-to-editor with the stringified id`() = runTest {
        val store = createStore(FakeRoomsRepository())
        val labels = mutableListOf<Label>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { store.labels.collect { labels.add(it) } }
        runCurrent()

        store.accept(Intent.OnRoomClick(roomId = 42L))
        advanceUntilIdle()

        assertEquals(listOf<Label>(Label.NavigateToRoomEditorExistingRoom(roomId = "42")), labels)

        store.dispose()
    }

    @Test
    fun `OnAddRoomClick publishes navigate-to-add-room`() = runTest {
        val store = createStore(FakeRoomsRepository())
        val labels = mutableListOf<Label>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { store.labels.collect { labels.add(it) } }
        runCurrent()

        store.accept(Intent.OnAddRoomClick)
        advanceUntilIdle()

        assertEquals(listOf<Label>(Label.NavigateToAddRoomScreen), labels)

        store.dispose()
    }
}
