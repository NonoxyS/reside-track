package dev.nonoxy.feature.manage_students.impl.domain

import dev.nonoxy.common.utils.currentLocalDate
import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsErrorKind
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.Intent
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.Label
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.State
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsSuccessKind
import dev.nonoxy.feature.manage_students.impl.domain.ManageStudentsStoreFactory.Action
import dev.nonoxy.feature.manage_students.impl.domain.ManageStudentsStoreFactory.Message
import dev.nonoxy.feature.rooms.api.models.Room
import dev.nonoxy.feature.rooms.api.models.Student
import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.residetrack.core.mvikotlin.BaseExecutor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, FormatStringsInDatetimeFormats::class)
internal class ManageStudentsExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
    private val mode: ManageStudentsMode,
) : BaseExecutor<Intent, Action, State, Message, Label>(mainContext = mainDispatcher) {

    private val dateDisplayFormat = LocalDate.Format {
        byUnicodePattern("dd.MM.yyyy")
    }

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.LoadInitial -> loadStudents()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            Intent.LoadStudents -> loadStudents()
            Intent.OnAddStudent -> handleAddStudent()
            is Intent.OnRemoveStudent -> handleRemoveStudent(intent.studentId)
            is Intent.OnStreamNumberChange -> handleStreamNumberChange(intent.studentId, intent.value)
            is Intent.OnCheckInDateChange -> handleCheckInDateChange(intent.studentId, intent.value)
            is Intent.OnCheckOutDateChange -> handleCheckOutDateChange(intent.studentId, intent.value)
            is Intent.OnCheckInDateMillisChange -> handleCheckInDateMillisChange(intent.studentId, intent.millis)
            is Intent.OnCheckOutDateMillisChange -> handleCheckOutDateMillisChange(intent.studentId, intent.millis)
            Intent.OnSaveAndClose -> handleSaveAndClose()
            Intent.OnClose -> publish(Label.NavigateBack)
        }
    }

    private suspend fun loadStudents() {
        dispatch(Message.SetIsLoading(isLoading = true))
        try {
            when (mode) {
                is ManageStudentsMode.ExistingRoom ->
                    loadFromResult { roomsRepository.getRoomById(mode.roomId.toLong()) }
                ManageStudentsMode.DraftRoom ->
                    loadFromResult { roomsRepository.getDraftRoom() }
            }
        } catch (_: Exception) {
            dispatch(Message.SetError(kind = ManageStudentsErrorKind.FailedToLoadStudents))
            publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToLoadStudents))
        }
    }

    private suspend fun loadFromResult(block: suspend () -> Result<Room?>) {
        block().onSuccess { room ->
            if (room == null) {
                val kind = when (mode) {
                    is ManageStudentsMode.ExistingRoom -> ManageStudentsErrorKind.RoomNotFound
                    ManageStudentsMode.DraftRoom -> ManageStudentsErrorKind.DraftRoomNotFound
                }
                dispatch(Message.SetError(kind = kind))
                return
            }

            val editableStudents = room.students.map { student ->
                State.EditableStudent(
                    id = Uuid.random().toString(),
                    studentId = student.id,
                    streamNumber = student.streamNumber.toString(),
                    checkInDate = dateDisplayFormat.format(student.checkInDate),
                    checkOutDate = dateDisplayFormat.format(student.checkOutDate),
                    checkInDateMillis = 1L,
                    checkOutDateMillis = 1L,
                    isNew = false,
                )
            }.toImmutableList()

            dispatch(
                Message.SetRoomAndStudents(
                    room = room,
                    editableStudents = editableStudents,
                )
            )
        }.onFailure {
            dispatch(Message.SetError(kind = ManageStudentsErrorKind.FailedToLoadStudents))
            publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToLoadStudents))
        }
    }

    private fun handleAddStudent() {
        val newStudent = State.EditableStudent(
            id = Uuid.random().toString(),
            studentId = null,
            streamNumber = "",
            checkInDate = "",
            checkOutDate = "",
            checkInDateMillis = null,
            checkOutDateMillis = null,
            isNew = true,
        )
        val updated = (state().editableStudents + newStudent).toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleRemoveStudent(studentId: String) {
        val updated = state().editableStudents
            .filterNot { it.id == studentId }
            .toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleStreamNumberChange(studentId: String, value: String) {
        val filtered = value.filter { it.isDigit() }
        val updated = state().editableStudents.map { st ->
            if (st.id == studentId) st.copy(streamNumber = filtered) else st
        }.toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleCheckInDateChange(studentId: String, value: String) {
        val updated = state().editableStudents.map { st ->
            if (st.id == studentId) st.copy(checkInDate = value, checkInDateMillis = parseDateToMillis(value)) else st
        }.toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleCheckOutDateChange(studentId: String, value: String) {
        val updated = state().editableStudents.map { st ->
            if (st.id == studentId) st.copy(checkOutDate = value, checkOutDateMillis = parseDateToMillis(value)) else st
        }.toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleCheckInDateMillisChange(studentId: String, millis: Long) {
        val dateString = formatDateFromMillis(millis)
        val updated = state().editableStudents.map { st ->
            if (st.id == studentId) st.copy(checkInDate = dateString, checkInDateMillis = millis) else st
        }.toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleCheckOutDateMillisChange(studentId: String, millis: Long) {
        val dateString = formatDateFromMillis(millis)
        val updated = state().editableStudents.map { st ->
            if (st.id == studentId) st.copy(checkOutDate = dateString, checkOutDateMillis = millis) else st
        }.toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private suspend fun handleSaveAndClose() {
        dispatch(Message.SetIsLoading(isLoading = true))

        val currentRoom = state().room
        if (currentRoom == null) {
            dispatch(Message.SetIsLoading(isLoading = false))
            publish(Label.ShowError(kind = ManageStudentsErrorKind.RoomNotFound))
            return
        }

        val students = try {
            buildStudentsOrPublishError() ?: run {
                dispatch(Message.SetIsLoading(isLoading = false))
                return
            }
        } catch (_: Exception) {
            dispatch(Message.SetIsLoading(isLoading = false))
            publish(Label.ShowError(kind = ManageStudentsErrorKind.InvalidDateFormat))
            return
        }

        val streamNumbers = students.map { it.streamNumber }
        if (streamNumbers.size != streamNumbers.distinct().size) {
            dispatch(Message.SetIsLoading(isLoading = false))
            publish(Label.ShowError(kind = ManageStudentsErrorKind.DuplicateStreamNumbers))
            return
        }

        when (mode) {
            is ManageStudentsMode.ExistingRoom -> saveExisting(currentRoom = currentRoom, students = students)
            ManageStudentsMode.DraftRoom -> saveDraft(currentRoom = currentRoom, students = students)
        }
    }

    /** Returns built list or null if a validation error was already published. */
    private suspend fun buildStudentsOrPublishError(): List<Student>? {
        val results = mutableListOf<Student>()
        for (editable in state().editableStudents) {
            if (editable.streamNumber.isBlank() ||
                editable.checkInDate.isBlank() ||
                editable.checkOutDate.isBlank()
            ) {
                continue
            }

            val streamNumber = editable.streamNumber.toIntOrNull()
            if (streamNumber == null) {
                publish(Label.ShowError(kind = ManageStudentsErrorKind.StreamNumberInvalid))
                return null
            }

            val checkInDate = dateDisplayFormat.parse(editable.checkInDate)
            val checkOutDate = dateDisplayFormat.parse(editable.checkOutDate)

            if (checkOutDate <= checkInDate) {
                publish(Label.ShowError(kind = ManageStudentsErrorKind.InvalidDateRange))
                return null
            }

            val currentDate = currentLocalDate
            val isNearOrExpired = checkOutDate.minus(currentDate).days <= 3

            results += Student(
                id = editable.studentId ?: 0L,
                streamNumber = streamNumber,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                isCheckOutDateNearOrExpired = isNearOrExpired,
            )
        }
        return results
    }

    private suspend fun saveExisting(currentRoom: Room, students: List<Student>) {
        roomsRepository.getRoomById((mode as ManageStudentsMode.ExistingRoom).roomId.toLong())
            .onSuccess { existingRoom ->
                if (existingRoom == null) {
                    dispatch(Message.SetIsLoading(isLoading = false))
                    publish(Label.ShowError(kind = ManageStudentsErrorKind.RoomNotFound))
                    return
                }
                val updated = existingRoom.copy(students = students)
                roomsRepository.saveRoom(updated)
                    .onSuccess {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowSuccess(kind = ManageStudentsSuccessKind.StudentsSaved))
                        publish(Label.NavigateBack)
                    }
                    .onFailure {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToSaveStudents))
                    }
            }
            .onFailure {
                dispatch(Message.SetIsLoading(isLoading = false))
                publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToSaveStudents))
            }
    }

    private suspend fun saveDraft(currentRoom: Room, students: List<Student>) {
        roomsRepository.getDraftRoom()
            .onSuccess { draftRoom ->
                if (draftRoom == null) {
                    dispatch(Message.SetIsLoading(isLoading = false))
                    publish(Label.ShowError(kind = ManageStudentsErrorKind.DraftRoomNotFound))
                    return
                }
                val updated = draftRoom.copy(students = students)
                roomsRepository.saveDraftRoom(updated)
                    .onSuccess {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowSuccess(kind = ManageStudentsSuccessKind.StudentsSaved))
                        publish(Label.NavigateBack)
                    }
                    .onFailure {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToSaveStudents))
                    }
            }
            .onFailure {
                dispatch(Message.SetIsLoading(isLoading = false))
                publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToSaveStudents))
            }
    }

    private fun formatDateFromMillis(millis: Long): String {
        val instant = Instant.fromEpochMilliseconds(millis)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return dateDisplayFormat.format(localDate)
    }

    private fun parseDateToMillis(dateString: String): Long? = try {
        val localDate = dateDisplayFormat.parse(dateString)
        localDate.toEpochDays().seconds.inWholeSeconds
    } catch (_: Exception) {
        null
    }
}
