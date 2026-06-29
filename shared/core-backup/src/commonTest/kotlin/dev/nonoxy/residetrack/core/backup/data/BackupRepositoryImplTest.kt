package dev.nonoxy.residetrack.core.backup.data

import dev.nonoxy.residetrack.core.backup.data.mapper.toBackup
import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.model.BackupRoom
import dev.nonoxy.residetrack.core.backup.domain.model.BackupStudent
import dev.nonoxy.residetrack.core.backup.domain.model.UnsupportedBackupVersionException
import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.core.database.relations.RoomWithStudents
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BackupRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun backup() = Backup(
        rooms = listOf(
            BackupRoom(
                floorNumber = 3,
                roomNumber = 329,
                bedsCount = 5,
                students = listOf(
                    BackupStudent(streamNumber = 12, checkInEpochMillis = 1000L, checkOutEpochMillis = 2000L),
                    BackupStudent(streamNumber = 13, checkInEpochMillis = 3000L, checkOutEpochMillis = 4000L),
                ),
            ),
            BackupRoom(floorNumber = 4, roomNumber = 401, bedsCount = 2, students = emptyList()),
        ),
    )

    private fun storedRows(): List<RoomWithStudents> = backup().rooms.map { room ->
        RoomWithStudents(
            room = RoomEntity(
                floorNumber = room.floorNumber,
                roomNumber = room.roomNumber,
                bedsCount = room.bedsCount,
            ),
            students = room.students.map { student ->
                StudentEntity(
                    roomId = 0,
                    streamNumber = student.streamNumber,
                    checkInDateEpochMillis = student.checkInEpochMillis,
                    checkOutDateEpochMillis = student.checkOutEpochMillis,
                )
            },
        )
    }

    private fun repository(storage: FakeRoomStorage) = BackupRepositoryImpl(roomStorage = storage, json = json)

    @Test
    fun `export then parse round-trips the backup`() = runTest {
        val source = backup()
        val repository = repository(FakeRoomStorage(stored = storedRows()))

        val exported = repository.export()
        assertTrue(exported.isSuccess)

        val parsed = repository.parse(exported.getOrThrow())
        assertEquals(source, parsed.getOrThrow())
    }

    @Test
    fun `exported json carries the current format version`() = runTest {
        val repository = repository(FakeRoomStorage(stored = storedRows()))

        val raw = repository.export().getOrThrow()

        assertTrue(raw.contains("\"version\":1"), "export must stamp version=1, was: $raw")
    }

    @Test
    fun `parse computes room and student counts`() = runTest {
        val repository = repository(FakeRoomStorage(stored = storedRows()))
        val raw = repository.export().getOrThrow()

        val parsed = repository.parse(raw).getOrThrow()

        assertEquals(2, parsed.roomCount)
        assertEquals(2, parsed.studentCount)
    }

    @Test
    fun `parse malformed json fails without throwing`() = runTest {
        val repository = repository(FakeRoomStorage())

        val result = repository.parse("{ not valid json")

        assertTrue(result.isFailure)
    }

    @Test
    fun `parse rejects an incompatible format version`() = runTest {
        val repository = repository(FakeRoomStorage())
        val futureFile = """{"version":999,"exportedAtEpochMillis":0,"rooms":[]}"""

        val result = repository.parse(futureFile)

        val error = result.exceptionOrNull()
        assertTrue(error is UnsupportedBackupVersionException)
        assertEquals(999, error.version)
    }

    @Test
    fun `restore writes the backup rows to storage`() = runTest {
        val storage = FakeRoomStorage()
        val repository = repository(storage)
        val source = backup()

        val result = repository.restore(source)

        assertTrue(result.isSuccess)
        val rebuilt = storage.replacedRooms!!.mapIndexed { index, room ->
            RoomWithStudents(room = room, students = storage.replacedStudentsByRoom!![index])
        }.toBackup()
        assertEquals(source, rebuilt)
    }

    @Test
    fun `restore is not triggered by parse`() = runTest {
        val storage = FakeRoomStorage()
        val repository = repository(storage)
        val raw = repository(FakeRoomStorage(stored = storedRows())).export().getOrThrow()

        repository.parse(raw)

        assertNull(storage.replacedRooms)
    }
}
