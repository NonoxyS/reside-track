package dev.nonoxy.residetrack.feature.rooms.impl.seed

import dev.nonoxy.residetrack.common.resources.FileResourceReader
import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.storage.RoomStorage
import dev.nonoxy.residetrack.core.initializer.Initializer
import dev.nonoxy.residetrack.res.MR
import kotlinx.serialization.json.Json

/** Runs once: if rooms already exist it no-ops, so user-managed students survive later launches. */
internal class RoomsSeedInitializer(
    private val roomStorage: RoomStorage,
    private val fileResourceReader: FileResourceReader,
    private val json: Json,
) : Initializer {

    override val priority: Int = SEED_PRIORITY

    override suspend fun initialize() {
        if (roomStorage.getRoomsCount() > 0) return

        val rawJson = fileResourceReader.readText(MR.files.rooms_seed_json)
        val rooms = json.decodeFromString<List<RoomSeed>>(rawJson)

        roomStorage.insertRooms(
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
