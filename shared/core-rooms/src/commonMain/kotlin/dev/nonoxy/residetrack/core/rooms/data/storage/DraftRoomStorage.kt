package dev.nonoxy.residetrack.core.rooms.data.storage

import dev.nonoxy.residetrack.core.rooms.domain.model.Room

/** Store for the single room being drafted while add-room hands off to the editor. */
internal interface DraftRoomStorage {

    fun save(room: Room)

    fun get(): Room?

    fun clear()
}
