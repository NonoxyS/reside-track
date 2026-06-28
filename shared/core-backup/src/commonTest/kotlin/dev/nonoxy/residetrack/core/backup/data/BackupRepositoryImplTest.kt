package dev.nonoxy.residetrack.core.backup.data

import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.model.BackupRoom
import dev.nonoxy.residetrack.core.backup.domain.model.BackupStudent
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

    private fun repository(gateway: FakeBackupGateway) = BackupRepositoryImpl(gateway = gateway, json = json)

    @Test
    fun `export then parse round-trips the backup`() = runTest {
        val source = backup()
        val repository = repository(FakeBackupGateway(stored = source))

        val exported = repository.export()
        assertTrue(exported.isSuccess)

        val parsed = repository.parse(exported.getOrThrow())
        assertEquals(source, parsed.getOrThrow())
    }

    @Test
    fun `exported json carries the current format version`() = runTest {
        val repository = repository(FakeBackupGateway(stored = backup()))

        val raw = repository.export().getOrThrow()

        assertTrue(raw.contains("\"version\":1"), "export must stamp version=1, was: $raw")
    }

    @Test
    fun `parse computes room and student counts`() = runTest {
        val repository = repository(FakeBackupGateway(stored = backup()))
        val raw = repository.export().getOrThrow()

        val parsed = repository.parse(raw).getOrThrow()

        assertEquals(2, parsed.roomCount)
        assertEquals(2, parsed.studentCount)
    }

    @Test
    fun `parse malformed json fails without throwing`() = runTest {
        val repository = repository(FakeBackupGateway())

        val result = repository.parse("{ not valid json")

        assertTrue(result.isFailure)
    }

    @Test
    fun `parse rejects an incompatible format version`() = runTest {
        val repository = repository(FakeBackupGateway())
        val futureFile = """{"version":999,"exportedAtEpochMillis":0,"rooms":[]}"""

        val result = repository.parse(futureFile)

        assertTrue(result.isFailure)
    }

    @Test
    fun `restore hands the backup to the gateway`() = runTest {
        val gateway = FakeBackupGateway()
        val repository = repository(gateway)
        val source = backup()

        val result = repository.restore(source)

        assertTrue(result.isSuccess)
        assertEquals(source, gateway.restoredWith)
    }

    @Test
    fun `restore is not triggered by parse`() = runTest {
        val gateway = FakeBackupGateway()
        val repository = repository(gateway)
        val raw = repository(FakeBackupGateway(stored = backup())).export().getOrThrow()

        repository.parse(raw)

        assertNull(gateway.restoredWith)
    }
}
