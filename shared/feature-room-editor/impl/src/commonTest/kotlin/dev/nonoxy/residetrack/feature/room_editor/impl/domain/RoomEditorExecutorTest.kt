package dev.nonoxy.residetrack.feature.room_editor.impl.domain

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.model.Student
import dev.nonoxy.residetrack.feature.room_editor.api.models.RoomEditorMode
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorErrorKind
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.Intent
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.Label
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorSuccessKind
import dev.nonoxy.residetrack.feature.room_editor.impl.data.FakeDraftRoomRepository
import dev.nonoxy.residetrack.feature.room_editor.impl.data.FakeRoomsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RoomEditorExecutorTest {

    private fun student(
        id: Long,
        stream: Int,
        checkIn: LocalDate = LocalDate(2030, 1, 1),
        checkOut: LocalDate = LocalDate(2030, 6, 1),
    ) = Student(
        id = id,
        streamNumber = stream,
        checkInDate = checkIn,
        checkOutDate = checkOut,
        isCheckOutDateNearOrExpired = false,
    )

    private fun room(
        id: Long,
        floor: Int = 2,
        number: Int = 21,
        beds: Int = 4,
        students: List<Student> = emptyList(),
    ) = Room(id = id, floorNumber = floor, roomNumber = number, bedsCount = beds, students = students)

    private fun TestScope.createStore(
        repository: FakeRoomsRepository,
        mode: RoomEditorMode,
        draftRepository: FakeDraftRoomRepository = FakeDraftRoomRepository(),
    ): RoomEditorStore = RoomEditorStoreFactory(
        storeFactory = DefaultStoreFactory(),
        mainDispatcher = UnconfinedTestDispatcher(testScheduler),
        roomsRepository = repository,
        draftRoomRepository = draftRepository,
    ).create(mode = mode)

    private fun TestScope.labelsOf(store: RoomEditorStore): List<Label> {
        val labels = mutableListOf<Label>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { store.labels.collect { labels.add(it) } }
        runCurrent()
        return labels
    }

    @Test
    fun `LoadInitial for an existing room maps its students into the editable buffer`() = runTest {
        val repository = FakeRoomsRepository(
            rooms = listOf(room(id = 1, students = listOf(student(id = 10, stream = 305)))),
        )

        val store = createStore(repository, RoomEditorMode.ExistingRoom(roomId = "1"))

        val state = store.state
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals(1, state.editableStudents.size)
        val editable = state.editableStudents.first()
        assertEquals("305", editable.streamNumber)
        assertEquals("01.01.2030", editable.checkInDate)
        assertEquals("01.06.2030", editable.checkOutDate)
        assertEquals(10L, editable.studentId)

        store.dispose()
    }

    @Test
    fun `LoadInitial sets RoomNotFound when an existing room is missing`() = runTest {
        val store = createStore(
            FakeRoomsRepository(getRoomByIdResult = Result.success(null)),
            RoomEditorMode.ExistingRoom(roomId = "404"),
        )

        assertTrue(store.state.isError)
        assertEquals(RoomEditorErrorKind.RoomNotFound, store.state.errorKind)

        store.dispose()
    }

    @Test
    fun `OnAddStudent appends a blank new student`() = runTest {
        val store = createStore(
            FakeRoomsRepository(rooms = listOf(room(id = 1))),
            RoomEditorMode.ExistingRoom(roomId = "1"),
        )

        store.accept(Intent.OnAddStudent)

        val students = store.state.editableStudents
        assertEquals(1, students.size)
        assertTrue(students.first().isNew)
        assertEquals("", students.first().streamNumber)
        assertTrue(store.state.isDirty)

        store.dispose()
    }

    @Test
    fun `remove student request then confirm drops it from the buffer`() = runTest {
        val store = createStore(
            FakeRoomsRepository(rooms = listOf(room(id = 1, students = listOf(student(id = 10, stream = 305))))),
            RoomEditorMode.ExistingRoom(roomId = "1"),
        )
        val id = store.state.editableStudents.first().id

        store.accept(Intent.OnRemoveStudentRequested(studentId = id))
        assertEquals(id, store.state.removingStudentId)

        store.accept(Intent.OnRemoveStudentConfirmed)
        assertTrue(store.state.editableStudents.isEmpty())
        assertNull(store.state.removingStudentId)

        store.dispose()
    }

    @Test
    fun `remove student dismiss keeps the student and clears the pending id`() = runTest {
        val store = createStore(
            FakeRoomsRepository(rooms = listOf(room(id = 1, students = listOf(student(id = 10, stream = 305))))),
            RoomEditorMode.ExistingRoom(roomId = "1"),
        )
        val id = store.state.editableStudents.first().id

        store.accept(Intent.OnRemoveStudentRequested(studentId = id))
        store.accept(Intent.OnRemoveStudentDismissed)

        assertNull(store.state.removingStudentId)
        assertEquals(1, store.state.editableStudents.size)

        store.dispose()
    }

    @Test
    fun `stream number change keeps digits only`() = runTest {
        val store = createStore(
            FakeRoomsRepository(rooms = listOf(room(id = 1, students = listOf(student(id = 10, stream = 305))))),
            RoomEditorMode.ExistingRoom(roomId = "1"),
        )
        val id = store.state.editableStudents.first().id

        store.accept(Intent.OnStreamNumberChange(studentId = id, value = "12a3"))

        assertEquals("123", store.state.editableStudents.first().streamNumber)

        store.dispose()
    }

    @Test
    fun `save existing persists the edited students and navigates back`() = runTest {
        val repository = FakeRoomsRepository(
            rooms = listOf(room(id = 1, students = listOf(student(id = 10, stream = 305)))),
        )
        val store = createStore(repository, RoomEditorMode.ExistingRoom(roomId = "1"))
        val labels = labelsOf(store)

        store.accept(Intent.OnSaveAndClose)
        advanceUntilIdle()

        assertEquals(1, repository.saveRoomCallCount)
        assertEquals(listOf(305), repository.savedRoom?.students?.map { it.streamNumber })
        assertEquals(10L, repository.savedRoom?.students?.first()?.id)
        assertTrue(labels.contains(Label.ShowSuccess(RoomEditorSuccessKind.StudentsSaved)))
        assertTrue(labels.contains(Label.NavigateBack))

        store.dispose()
    }

    @Test
    fun `save with duplicate stream numbers is rejected`() = runTest {
        val repository = FakeRoomsRepository(
            rooms = listOf(
                room(
                    id = 1,
                    students = listOf(student(id = 10, stream = 305), student(id = 11, stream = 305)),
                ),
            ),
        )
        val store = createStore(repository, RoomEditorMode.ExistingRoom(roomId = "1"))
        val labels = labelsOf(store)

        store.accept(Intent.OnSaveAndClose)
        advanceUntilIdle()

        assertEquals(0, repository.saveRoomCallCount)
        assertTrue(labels.contains(Label.ShowError(RoomEditorErrorKind.DuplicateStreamNumbers)))

        store.dispose()
    }

    @Test
    fun `save with check-out before check-in reports an invalid range`() = runTest {
        val repository = FakeRoomsRepository(
            rooms = listOf(room(id = 1, students = listOf(student(id = 10, stream = 305)))),
        )
        val store = createStore(repository, RoomEditorMode.ExistingRoom(roomId = "1"))
        val labels = labelsOf(store)
        val id = store.state.editableStudents.first().id

        store.accept(Intent.OnCheckInDateChange(studentId = id, value = "10.06.2030"))
        store.accept(Intent.OnCheckOutDateChange(studentId = id, value = "01.06.2030"))
        store.accept(Intent.OnSaveAndClose)
        advanceUntilIdle()

        assertEquals(0, repository.saveRoomCallCount)
        assertTrue(labels.contains(Label.ShowError(RoomEditorErrorKind.InvalidDateRange)))

        store.dispose()
    }

    @Test
    fun `save draft persists students clears the draft and navigates back`() = runTest {
        val repository = FakeRoomsRepository()
        val draftRepository = FakeDraftRoomRepository(
            draftRoom = room(id = 9, floor = 1, number = 101, students = listOf(student(id = 0, stream = 305))),
        )
        val store = createStore(repository, RoomEditorMode.DraftRoom, draftRepository)
        val labels = labelsOf(store)

        store.accept(Intent.OnSaveAndClose)
        advanceUntilIdle()

        assertEquals(1, repository.saveRoomCallCount)
        assertEquals(1, draftRepository.clearCallCount)
        assertEquals(listOf(305), repository.savedRoom?.students?.map { it.streamNumber })
        assertTrue(labels.contains(Label.NavigateBack))

        store.dispose()
    }

    @Test
    fun `edit room params opens the sheet seeded from the loaded room`() = runTest {
        val store = createStore(
            FakeRoomsRepository(rooms = listOf(room(id = 1, floor = 2, number = 21, beds = 4))),
            RoomEditorMode.ExistingRoom(roomId = "1"),
        )

        store.accept(Intent.OnEditRoomParamsClick)

        val params = store.state.roomParams
        assertTrue(store.state.showRoomParams)
        assertEquals("2", params?.floorNumber)
        assertEquals("21", params?.roomNumber)
        assertEquals("4", params?.bedsCount)

        store.dispose()
    }

    @Test
    fun `save room params updates metadata and reflects it in state`() = runTest {
        val repository = FakeRoomsRepository(rooms = listOf(room(id = 1, floor = 2, number = 21, beds = 4)))
        val store = createStore(repository, RoomEditorMode.ExistingRoom(roomId = "1"))
        val labels = labelsOf(store)

        store.accept(Intent.OnEditRoomParamsClick)
        store.accept(Intent.OnRoomParamsFloorChange(value = "5"))
        store.accept(Intent.OnRoomParamsRoomNumberChange(value = "55"))
        store.accept(Intent.OnRoomParamsBedsChange(value = "6"))
        store.accept(Intent.OnSaveRoomParams)
        advanceUntilIdle()

        assertEquals(FakeRoomsRepository.Metadata(1L, 5, 55, 6), repository.updatedMetadata)
        assertFalse(store.state.showRoomParams)
        assertEquals(5, store.state.room?.floorNumber)
        assertEquals(55, store.state.room?.roomNumber)
        assertTrue(labels.contains(Label.ShowSuccess(RoomEditorSuccessKind.RoomUpdated)))

        store.dispose()
    }

    @Test
    fun `save room params reports a taken number against another room`() = runTest {
        val repository = FakeRoomsRepository(
            rooms = listOf(
                room(id = 1, floor = 2, number = 21, beds = 4),
                room(id = 2, floor = 5, number = 55, beds = 4),
            ),
        )
        val store = createStore(repository, RoomEditorMode.ExistingRoom(roomId = "1"))
        val labels = labelsOf(store)

        store.accept(Intent.OnEditRoomParamsClick)
        store.accept(Intent.OnRoomParamsFloorChange(value = "5"))
        store.accept(Intent.OnRoomParamsRoomNumberChange(value = "55"))
        store.accept(Intent.OnSaveRoomParams)
        advanceUntilIdle()

        assertNull(repository.updatedMetadata)
        assertTrue(store.state.roomParams?.roomNumberError == true)
        assertTrue(labels.contains(Label.ShowError(RoomEditorErrorKind.RoomNumberTaken)))

        store.dispose()
    }

    @Test
    fun `delete room confirm deletes it and navigates back`() = runTest {
        val repository = FakeRoomsRepository(rooms = listOf(room(id = 1)))
        val store = createStore(repository, RoomEditorMode.ExistingRoom(roomId = "1"))
        val labels = labelsOf(store)

        store.accept(Intent.OnDeleteRoomClick)
        assertTrue(store.state.showDeleteConfirm)

        store.accept(Intent.OnDeleteRoomConfirm)
        advanceUntilIdle()

        assertEquals(1L, repository.deletedRoomId)
        assertTrue(labels.contains(Label.ShowSuccess(RoomEditorSuccessKind.RoomDeleted)))
        assertTrue(labels.contains(Label.NavigateBack))

        store.dispose()
    }

    @Test
    fun `room params and delete are no-ops in draft mode`() = runTest {
        val repository = FakeRoomsRepository()
        val draftRepository = FakeDraftRoomRepository(draftRoom = room(id = 9, students = emptyList()))
        val store = createStore(repository, RoomEditorMode.DraftRoom, draftRepository)

        store.accept(Intent.OnSaveRoomParams)
        store.accept(Intent.OnDeleteRoomConfirm)
        advanceUntilIdle()

        assertNull(repository.updatedMetadata)
        assertNull(repository.deletedRoomId)

        store.dispose()
    }
}
