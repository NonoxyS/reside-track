package dev.nonoxy.residetrack.core.rooms.domain.repository

import dev.nonoxy.residetrack.core.rooms.domain.model.Room

/**
 * Handoff for the single room being drafted while add-room passes it to the
 * editor. Backed by an in-memory store hidden behind this port.
 */
interface DraftRoomRepository {

    suspend fun save(room: Room): Result<Unit>

    suspend fun get(): Result<Room?>

    suspend fun clear(): Result<Unit>
}
