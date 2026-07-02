package dev.nonoxy.residetrack.core.notifications.domain

import dev.nonoxy.residetrack.core.database.relations.RoomWithStudents
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Чистая функция: свёртка комнат со студентами в список дайджестов выезда для планирования пушей.
 *
 * Группировка по дате выезда (один пуш на дату, не на студента). Пуши, чей момент показа
 * уже в прошлом, отбрасываются — такие выезды и так видны в Upcoming.
 */
object DigestBuilder {

    /** За сколько дней до выезда показываем пуш. */
    const val LEAD_DAYS = 3

    /** Время показа пуша (local). */
    val fireTime = LocalTime(hour = 9, minute = 0)

    fun build(
        rooms: List<RoomWithStudents>,
        now: Instant,
        timeZone: TimeZone,
    ): List<CheckoutDigest> = rooms
        .flatMap { roomWithStudents ->
            roomWithStudents.students.map { student ->
                roomWithStudents.room.roomNumber to student.checkOutDateEpochMillis.toLocalDate(timeZone)
            }
        }
        .groupBy(keySelector = { (_, date) -> date }, valueTransform = { (roomNumber, _) -> roomNumber })
        .mapNotNull { (date, roomNumbers) ->
            val fireAt = fireInstant(date, timeZone)
            if (fireAt < now) return@mapNotNull null

            CheckoutDigest(
                date = date,
                roomNumbers = roomNumbers.distinct().sorted(),
                placesCount = roomNumbers.size,
                fireAtEpochMillis = fireAt.toEpochMilliseconds(),
            )
        }
        .sortedBy { digest -> digest.date }

    private fun fireInstant(checkOutDate: LocalDate, timeZone: TimeZone): Instant {
        val fireDate = checkOutDate.minus(DatePeriod(days = LEAD_DAYS))
        return LocalDateTime(date = fireDate, time = fireTime).toInstant(timeZone)
    }

    private fun Long.toLocalDate(timeZone: TimeZone): LocalDate =
        Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone).date
}
