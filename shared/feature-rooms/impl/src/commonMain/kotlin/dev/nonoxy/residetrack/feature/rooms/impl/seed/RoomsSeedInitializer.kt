package dev.nonoxy.residetrack.feature.rooms.impl.seed

import dev.nonoxy.residetrack.common.resources.FileResourceReader
import dev.nonoxy.residetrack.core.database.dao.RoomDao
import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.initializer.Initializer
import dev.nonoxy.residetrack.res.MR
import kotlinx.serialization.json.Json

/**
 * Seeds the fixed building structure (floors / rooms / beds) on first launch.
 *
 * The room layout is known up front and ships as `rooms_seed.json`. This runs once: if the table
 * already holds rooms it does nothing, so user-managed students are never touched on later launches.
 */
internal class RoomsSeedInitializer(
    private val roomDao: RoomDao,
    private val fileResourceReader: FileResourceReader,
    private val json: Json,
) : Initializer {

    override val priority: Int = SEED_PRIORITY

    override suspend fun initialize() {
        if (roomDao.getRoomsCount() > 0) return

        val rawJson = fileResourceReader.readText(MR.files.rooms_seed_json)
        val rooms = json.decodeFromString<List<RoomSeed>>(rawJson)

        roomDao.insertRooms(
            rooms.map { seed ->
                RoomEntity(
                    floorNumber = seed.floor,
                    roomNumber = seed.number,
                    bedsCount = seed.beds,
                )
            }
        )
    }

    private companion object {
        const val SEED_PRIORITY = 0
    }
}
