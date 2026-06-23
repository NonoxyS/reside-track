package dev.nonoxy.residetrack.feature.add_room.impl.domain

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.nonoxy.residetrack.core.rooms.models.Room
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore.Intent
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore.Label
import dev.nonoxy.residetrack.feature.add_room.impl.data.FakeRoomsRepository
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
class AddRoomExecutorTest {

    private fun room(id: Long, floor: Int, number: Int, beds: Int) =
        Room(id = id, floorNumber = floor, roomNumber = number, bedsCount = beds, students = emptyList())

    private fun TestScope.createStore(repository: FakeRoomsRepository): AddRoomStore =
        AddRoomStoreFactory(
            storeFactory = DefaultStoreFactory(),
            mainDispatcher = UnconfinedTestDispatcher(testScheduler),
            roomsRepository = repository,
        ).create()

    private fun TestScope.labelsOf(store: AddRoomStore): List<Label> {
        val labels = mutableListOf<Label>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { store.labels.collect { labels.add(it) } }
        runCurrent()
        return labels
    }

    private fun AddRoomStore.fillForm(floor: String, roomNumber: String, beds: String) {
        accept(Intent.OnFloorNumberInputValueChange(floor))
        accept(Intent.OnRoomNumberInputValueChange(roomNumber))
        accept(Intent.OnBedsCountInputValueChange(beds))
    }

    @Test
    fun `LoadExistingRooms exposes distinct sorted floors and beds`() = runTest {
        val repository = FakeRoomsRepository(
            rooms = listOf(
                room(id = 1, floor = 3, number = 31, beds = 4),
                room(id = 2, floor = 1, number = 11, beds = 2),
                room(id = 3, floor = 3, number = 32, beds = 4),
            ),
        )

        val store = createStore(repository)

        val state = store.state
        assertTrue(state.hasExistingRooms)
        assertEquals(listOf(1, 3), state.floorSelection.existingFloors)
        assertEquals(listOf(2, 4), state.bedsSelection.existingBedsCounts)

        store.dispose()
    }

    @Test
    fun `no existing rooms keeps hasExistingRooms false`() = runTest {
        val store = createStore(FakeRoomsRepository())

        assertFalse(store.state.hasExistingRooms)

        store.dispose()
    }

    @Test
    fun `input ignores non-digits and values longer than four chars`() = runTest {
        val store = createStore(FakeRoomsRepository())

        store.accept(Intent.OnFloorNumberInputValueChange("12"))
        assertEquals("12", store.state.floorSelection.textField.value)

        store.accept(Intent.OnFloorNumberInputValueChange("12a"))
        assertEquals("12", store.state.floorSelection.textField.value) // rejected, unchanged

        store.accept(Intent.OnFloorNumberInputValueChange("12345"))
        assertEquals("12", store.state.floorSelection.textField.value) // too long, unchanged

        store.dispose()
    }

    @Test
    fun `form becomes valid once all fields are filled`() = runTest {
        val store = createStore(FakeRoomsRepository())
        assertFalse(store.state.isFormValid)

        store.fillForm(floor = "1", roomNumber = "101", beds = "4")

        assertTrue(store.state.isFormValid)

        store.dispose()
    }

    @Test
    fun `create with blank fields surfaces required errors and does not save`() = runTest {
        val repository = FakeRoomsRepository()
        val store = createStore(repository)

        store.accept(Intent.OnCreateRoomClick)

        val state = store.state
        assertEquals(AddRoomErrorKind.FloorNumberRequired, state.floorSelection.textField.errorKind)
        assertEquals(AddRoomErrorKind.RoomNumberRequired, state.roomNumber.errorKind)
        assertEquals(AddRoomErrorKind.BedsCountRequired, state.bedsSelection.textField.errorKind)
        assertFalse(state.isFormValid)
        assertFalse(state.isLoading)
        assertEquals(0, repository.saveRoomCallCount)

        store.dispose()
    }

    @Test
    fun `create with a duplicate floor and room number reports RoomAlreadyExists`() = runTest {
        val repository = FakeRoomsRepository(rooms = listOf(room(id = 1, floor = 2, number = 21, beds = 4)))
        val store = createStore(repository)
        val labels = labelsOf(store)

        store.fillForm(floor = "2", roomNumber = "21", beds = "4")
        store.accept(Intent.OnCreateRoomClick)
        advanceUntilIdle()

        assertEquals(
            listOf(AddRoomErrorKind.RoomAlreadyExists(roomNumber = 21, floorNumber = 2)),
            labels.filterIsInstance<Label.ShowError>().map { it.kind },
        )
        assertEquals(0, repository.saveRoomCallCount)
        assertFalse(store.state.isLoading)

        store.dispose()
    }

    @Test
    fun `create success saves room and draft then navigates to draft editor`() = runTest {
        val repository = FakeRoomsRepository(saveRoomResult = Result.success(7L))
        val store = createStore(repository)
        val labels = labelsOf(store)

        store.fillForm(floor = "1", roomNumber = "101", beds = "4")
        store.accept(Intent.OnCreateRoomClick)
        advanceUntilIdle()

        assertEquals(1, repository.saveRoomCallCount)
        assertEquals(1, repository.savedRoom?.floorNumber)
        assertEquals(101, repository.savedRoom?.roomNumber)
        assertEquals(4, repository.savedRoom?.bedsCount)
        assertEquals(7L, repository.savedDraftRoom?.id) // persisted id propagated into the draft
        assertTrue(labels.contains(Label.NavigateToRoomEditorDraftRoom))
        assertFalse(store.state.isLoading)

        store.dispose()
    }

    @Test
    fun `create reports SaveFailed when persisting the room fails`() = runTest {
        val repository = FakeRoomsRepository(saveRoomResult = Result.failure(RuntimeException("disk full")))
        val store = createStore(repository)
        val labels = labelsOf(store)

        store.fillForm(floor = "1", roomNumber = "101", beds = "4")
        store.accept(Intent.OnCreateRoomClick)
        advanceUntilIdle()

        assertEquals(
            listOf(AddRoomErrorKind.SaveFailed),
            labels.filterIsInstance<Label.ShowError>().map { it.kind },
        )
        assertFalse(store.state.isLoading)

        store.dispose()
    }

    @Test
    fun `dismiss with entered input asks to confirm discard`() = runTest {
        val store = createStore(FakeRoomsRepository())
        store.accept(Intent.OnFloorNumberInputValueChange("3"))

        store.accept(Intent.OnDismissRequested)

        assertTrue(store.state.showDiscardConfirm)

        store.dispose()
    }

    @Test
    fun `dismiss with a clean form closes the screen immediately`() = runTest {
        val store = createStore(FakeRoomsRepository())
        val labels = labelsOf(store)

        store.accept(Intent.OnDismissRequested)
        advanceUntilIdle()

        assertFalse(store.state.showDiscardConfirm)
        assertEquals(listOf(Label.CloseScreen), labels)

        store.dispose()
    }
}
