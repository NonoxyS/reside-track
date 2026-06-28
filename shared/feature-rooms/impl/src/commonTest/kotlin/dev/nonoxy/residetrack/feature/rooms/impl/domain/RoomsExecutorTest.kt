package dev.nonoxy.residetrack.feature.rooms.impl.domain

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.model.BackupRoom
import dev.nonoxy.residetrack.core.backup.domain.model.UnsupportedBackupVersionException
import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.feature.rooms.api.store.BackupErrorKind
import dev.nonoxy.residetrack.feature.rooms.api.store.BackupSuccessKind
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.residetrack.feature.rooms.impl.data.FakeBackupRepository
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RoomsExecutorTest {

    private fun room(id: Long, floor: Int, number: Int, beds: Int = 4) =
        Room(id = id, floorNumber = floor, roomNumber = number, bedsCount = beds, students = emptyList())

    private fun TestScope.createStore(
        repository: FakeRoomsRepository,
        backupRepository: FakeBackupRepository = FakeBackupRepository(),
    ): RoomsStore =
        RoomsStoreFactory(
            storeFactory = DefaultStoreFactory(),
            mainDispatcher = UnconfinedTestDispatcher(testScheduler),
            roomsRepository = repository,
            backupRepository = backupRepository,
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
    fun `LoadInitial sorts floors ascending and rooms by number within a floor`() = runTest {
        val repository = FakeRoomsRepository(
            rooms = listOf(
                room(id = 1, floor = 3, number = 32),
                room(id = 2, floor = 1, number = 12),
                room(id = 3, floor = 1, number = 11),
                room(id = 4, floor = 2, number = 21),
            ),
        )

        val store = createStore(repository)

        val state = store.state
        assertEquals(listOf(1, 2, 3), state.roomsOnFloor.keys.toList())
        assertEquals(listOf(11, 12), state.roomsOnFloor.getValue(1).map { room -> room.roomNumber })

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

    private val sampleBackup = Backup(
        rooms = listOf(BackupRoom(floorNumber = 3, roomNumber = 329, bedsCount = 5, students = emptyList())),
    )

    private fun TestScope.collectLabels(store: RoomsStore): MutableList<Label> {
        val labels = mutableListOf<Label>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { store.labels.collect { labels.add(it) } }
        runCurrent()
        return labels
    }

    @Test
    fun `OnExportClick on success asks UI to save the exported file`() = runTest {
        val backup = FakeBackupRepository(exportResult = Result.success("{\"version\":1}"))
        val store = createStore(FakeRoomsRepository(rooms = listOf(room(id = 1, floor = 3, number = 329))), backup)
        val labels = collectLabels(store)

        store.accept(Intent.OnExportClick)
        advanceUntilIdle()

        val label = labels.single()
        assertTrue(label is Label.SaveBackupFile)
        assertEquals("{\"version\":1}", label.json)
        assertTrue(label.suggestedName.startsWith("reside-track-backup-"))
        assertTrue(label.suggestedName.endsWith(".json"))

        store.dispose()
    }

    @Test
    fun `OnExportClick on failure shows an export error`() = runTest {
        val backup = FakeBackupRepository(exportResult = Result.failure(RuntimeException("boom")))
        val store = createStore(FakeRoomsRepository(rooms = listOf(room(id = 1, floor = 3, number = 329))), backup)
        val labels = collectLabels(store)

        store.accept(Intent.OnExportClick)
        advanceUntilIdle()

        assertEquals(listOf<Label>(Label.ShowBackupError(BackupErrorKind.ExportFailed)), labels)

        store.dispose()
    }

    @Test
    fun `OnExportCompleted maps success and failure to the right labels`() = runTest {
        val store = createStore(FakeRoomsRepository())
        val labels = collectLabels(store)

        store.accept(Intent.OnExportCompleted(success = true))
        store.accept(Intent.OnExportCompleted(success = false))
        advanceUntilIdle()

        assertEquals(
            listOf<Label>(
                Label.ShowBackupSuccess(BackupSuccessKind.Exported),
                Label.ShowBackupError(BackupErrorKind.ExportFailed),
            ),
            labels,
        )

        store.dispose()
    }

    @Test
    fun `OnImportClick asks UI to pick a backup file`() = runTest {
        val store = createStore(FakeRoomsRepository())
        val labels = collectLabels(store)

        store.accept(Intent.OnImportClick)
        advanceUntilIdle()

        assertEquals(listOf<Label>(Label.OpenBackupFile), labels)

        store.dispose()
    }

    @Test
    fun `OnBackupFileLoaded with a valid file opens the confirmation`() = runTest {
        val backup = FakeBackupRepository(parseResult = Result.success(sampleBackup))
        val store = createStore(FakeRoomsRepository(), backup)

        store.accept(Intent.OnBackupFileLoaded(json = "{}"))
        advanceUntilIdle()

        assertEquals(sampleBackup, store.state.importConfirmation)

        store.dispose()
    }

    @Test
    fun `OnBackupFileLoaded with an unreadable file shows an error and no confirmation`() = runTest {
        val backup = FakeBackupRepository(parseResult = Result.failure(RuntimeException("bad")))
        val store = createStore(FakeRoomsRepository(), backup)
        val labels = collectLabels(store)

        store.accept(Intent.OnBackupFileLoaded(json = "garbage"))
        advanceUntilIdle()

        assertEquals(listOf<Label>(Label.ShowBackupError(BackupErrorKind.ImportReadFailed)), labels)
        assertNull(store.state.importConfirmation)

        store.dispose()
    }

    @Test
    fun `OnBackupFileLoaded with an unsupported version shows a version error`() = runTest {
        val backup = FakeBackupRepository(parseResult = Result.failure(UnsupportedBackupVersionException(999)))
        val store = createStore(FakeRoomsRepository(), backup)
        val labels = collectLabels(store)

        store.accept(Intent.OnBackupFileLoaded(json = "{\"version\":999}"))
        advanceUntilIdle()

        assertEquals(listOf<Label>(Label.ShowBackupError(BackupErrorKind.ImportVersionUnsupported)), labels)
        assertNull(store.state.importConfirmation)

        store.dispose()
    }

    @Test
    fun `OnRestoreConfirm restores and clears the confirmation`() = runTest {
        val backup = FakeBackupRepository(parseResult = Result.success(sampleBackup))
        val store = createStore(FakeRoomsRepository(), backup)
        val labels = collectLabels(store)
        store.accept(Intent.OnBackupFileLoaded(json = "{}"))
        advanceUntilIdle()

        store.accept(Intent.OnRestoreConfirm)
        advanceUntilIdle()

        assertEquals(sampleBackup, backup.restoreCalledWith)
        assertEquals(listOf<Label>(Label.ShowBackupSuccess(BackupSuccessKind.Restored)), labels)
        assertNull(store.state.importConfirmation)

        store.dispose()
    }

    @Test
    fun `OnRestoreConfirm failure clears loading and shows a restore error`() = runTest {
        val backup = FakeBackupRepository(
            parseResult = Result.success(sampleBackup),
            restoreResult = Result.failure(RuntimeException("disk full")),
        )
        val store = createStore(FakeRoomsRepository(), backup)
        val labels = collectLabels(store)
        store.accept(Intent.OnBackupFileLoaded(json = "{}"))
        advanceUntilIdle()

        store.accept(Intent.OnRestoreConfirm)
        advanceUntilIdle()

        assertEquals(listOf<Label>(Label.ShowBackupError(BackupErrorKind.RestoreFailed)), labels)
        assertFalse(store.state.isLoading)
        assertNull(store.state.importConfirmation)

        store.dispose()
    }

    @Test
    fun `OnRestoreCancel clears the confirmation without restoring`() = runTest {
        val backup = FakeBackupRepository(parseResult = Result.success(sampleBackup))
        val store = createStore(FakeRoomsRepository(), backup)
        store.accept(Intent.OnBackupFileLoaded(json = "{}"))
        advanceUntilIdle()

        store.accept(Intent.OnRestoreCancel)
        advanceUntilIdle()

        assertNull(store.state.importConfirmation)
        assertNull(backup.restoreCalledWith)

        store.dispose()
    }

    @Test
    fun `OnExportClick with an empty database refuses without exporting`() = runTest {
        val backup = FakeBackupRepository(exportResult = Result.success("{\"version\":1}"))
        val store = createStore(FakeRoomsRepository(rooms = emptyList()), backup)
        val labels = collectLabels(store)

        store.accept(Intent.OnExportClick)
        advanceUntilIdle()

        assertEquals(listOf<Label>(Label.ShowBackupError(BackupErrorKind.ExportNoData)), labels)

        store.dispose()
    }

    @Test
    fun `OnBackupFileLoaded with an empty backup is refused and shows no confirmation`() = runTest {
        val backup = FakeBackupRepository(parseResult = Result.success(Backup(rooms = emptyList())))
        val store = createStore(FakeRoomsRepository(), backup)
        val labels = collectLabels(store)

        store.accept(Intent.OnBackupFileLoaded(json = "{}"))
        advanceUntilIdle()

        assertEquals(listOf<Label>(Label.ShowBackupError(BackupErrorKind.ImportEmpty)), labels)
        assertNull(store.state.importConfirmation)

        store.dispose()
    }
}
