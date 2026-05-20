package dev.nonoxy.feature.rooms.models

data class Room(
    val id: Long = 0,
    val floorNumber: Int,
    val roomNumber: Int,
    val bedsCount: Int,
    val students: List<Student>
)
