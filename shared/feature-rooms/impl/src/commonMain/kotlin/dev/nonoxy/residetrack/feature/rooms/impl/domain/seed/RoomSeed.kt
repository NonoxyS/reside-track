package dev.nonoxy.residetrack.feature.rooms.impl.domain.seed

import kotlinx.serialization.Serializable

/** One row of the prepopulated room structure in `rooms_seed.json`. */
@Serializable
internal data class RoomSeed(
    val floor: Int,
    val number: Int,
    val beds: Int,
)
