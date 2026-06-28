package dev.nonoxy.residetrack.core.rooms.domain.model

data class Room(
    val id: Long = 0,
    val floorNumber: Int,
    val roomNumber: Int,
    val bedsCount: Int,
    val students: List<Student>
)
