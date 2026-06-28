package dev.nonoxy.residetrack.feature.room_editor.impl.domain

import dev.nonoxy.residetrack.common.utils.currentLocalDate
import dev.nonoxy.residetrack.feature.room_editor.api.models.RoomEditorMode
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorErrorKind
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.Intent
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.Label
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.State
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorSuccessKind
import dev.nonoxy.residetrack.feature.room_editor.impl.domain.RoomEditorStoreFactory.Action
import dev.nonoxy.residetrack.feature.room_editor.impl.domain.RoomEditorStoreFactory.Message
import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.model.Student
import dev.nonoxy.residetrack.core.rooms.domain.repository.RoomsRepository
import dev.nonoxy.residetrack.core.rooms.domain.validation.RoomNumberConflict
import dev.nonoxy.residetrack.core.mvikotlin.BaseExecutor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, FormatStringsInDatetimeFormats::class)
internal class RoomEditorExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
    private val mode: RoomEditorMode,
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
            is Intent.OnRemoveStudentRequested ->
                dispatch(Message.SetRemovingStudentId(intent.studentId))
            Intent.OnRemoveStudentConfirmed -> handleRemoveStudentConfirmed()
            Intent.OnRemoveStudentDismissed -> dispatch(Message.SetRemovingStudentId(null))
            is Intent.OnStreamNumberChange -> handleStreamNumberChange(intent.studentId, intent.value)
            is Intent.OnCheckInDateChange -> handleCheckInDateChange(intent.studentId, intent.value)
            is Intent.OnCheckOutDateChange -> handleCheckOutDateChange(intent.studentId, intent.value)
            is Intent.OnCheckInDateMillisChange -> handleCheckInDateMillisChange(intent.studentId, intent.millis)
            is Intent.OnCheckOutDateMillisChange -> handleCheckOutDateMillisChange(intent.studentId, intent.millis)
            is Intent.OnDatePickerOpen ->
                dispatch(Message.SetOpenDatePicker(State.OpenPicker(intent.studentId, intent.field)))
            Intent.OnDatePickerDismiss -> dispatch(Message.SetOpenDatePicker(null))
            Intent.OnSaveAndClose -> handleSaveAndClose()
            Intent.OnClose -> publish(Label.NavigateBack)
            Intent.OnDismissRequested ->
                if (state().isDirty) {
                    dispatch(Message.SetShowDiscardConfirm(show = true))
                } else {
                    publish(Label.NavigateBack)
                }
            Intent.OnDiscardConfirmed -> publish(Label.NavigateBack)
            Intent.OnKeepEditing -> dispatch(Message.SetShowDiscardConfirm(show = false))
            Intent.OnEditRoomParamsClick -> handleEditRoomParamsClick()
            Intent.OnRoomParamsDismiss -> dispatch(Message.SetShowRoomParams(show = false))
            is Intent.OnRoomParamsFloorChange -> updateRoomParam { it.copy(floorNumber = digits(intent.value)) }
            is Intent.OnRoomParamsRoomNumberChange ->
                updateRoomParam { it.copy(roomNumber = digits(intent.value), roomNumberError = false) }
            is Intent.OnRoomParamsBedsChange -> updateRoomParam { it.copy(bedsCount = digits(intent.value)) }
            Intent.OnSaveRoomParams -> handleSaveRoomParams()
            Intent.OnDeleteRoomClick -> dispatch(Message.SetShowDeleteConfirm(show = true))
            Intent.OnDeleteRoomDismiss -> dispatch(Message.SetShowDeleteConfirm(show = false))
            Intent.OnDeleteRoomConfirm -> handleDeleteRoom()
        }
    }

    private suspend fun loadStudents() {
        dispatch(Message.SetIsLoading(isLoading = true))
        try {
            when (mode) {
                is RoomEditorMode.ExistingRoom ->
                    loadFromResult { roomsRepository.getRoomById(mode.roomId.toLong()) }
                RoomEditorMode.DraftRoom ->
                    loadFromResult { roomsRepository.getDraftRoom() }
            }
        } catch (_: Exception) {
            dispatch(Message.SetError(kind = RoomEditorErrorKind.FailedToLoadStudents))
            publish(Label.ShowError(kind = RoomEditorErrorKind.FailedToLoadStudents))
        }
    }

    private suspend fun loadFromResult(block: suspend () -> Result<Room?>) {
        block().onSuccess { room ->
            if (room == null) {
                val kind = when (mode) {
                    is RoomEditorMode.ExistingRoom -> RoomEditorErrorKind.RoomNotFound
                    RoomEditorMode.DraftRoom -> RoomEditorErrorKind.DraftRoomNotFound
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
                    checkInDateMillis = student.checkInDate.toUtcMillis(),
                    checkOutDateMillis = student.checkOutDate.toUtcMillis(),
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
            dispatch(Message.SetError(kind = RoomEditorErrorKind.FailedToLoadStudents))
            publish(Label.ShowError(kind = RoomEditorErrorKind.FailedToLoadStudents))
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

    private fun handleRemoveStudentConfirmed() {
        val studentId = state().removingStudentId ?: return
        val updated = state().editableStudents
            .filterNot { it.id == studentId }
            .toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
        dispatch(Message.SetRemovingStudentId(null))
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
            publish(Label.ShowError(kind = RoomEditorErrorKind.RoomNotFound))
            return
        }

        val students = try {
            buildStudentsOrPublishError() ?: run {
                dispatch(Message.SetIsLoading(isLoading = false))
                return
            }
        } catch (_: Exception) {
            dispatch(Message.SetIsLoading(isLoading = false))
            publish(Label.ShowError(kind = RoomEditorErrorKind.InvalidDateFormat))
            return
        }

        val streamNumbers = students.map { it.streamNumber }
        if (streamNumbers.size != streamNumbers.distinct().size) {
            dispatch(Message.SetIsLoading(isLoading = false))
            publish(Label.ShowError(kind = RoomEditorErrorKind.DuplicateStreamNumbers))
            return
        }

        when (mode) {
            is RoomEditorMode.ExistingRoom -> saveExisting(currentRoom = currentRoom, students = students)
            RoomEditorMode.DraftRoom -> saveDraft(currentRoom = currentRoom, students = students)
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
                publish(Label.ShowError(kind = RoomEditorErrorKind.StreamNumberInvalid))
                return null
            }

            val checkInDate = dateDisplayFormat.parse(editable.checkInDate)
            val checkOutDate = dateDisplayFormat.parse(editable.checkOutDate)

            if (checkOutDate <= checkInDate) {
                publish(Label.ShowError(kind = RoomEditorErrorKind.InvalidDateRange))
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
        roomsRepository.getRoomById((mode as RoomEditorMode.ExistingRoom).roomId.toLong())
            .onSuccess { existingRoom ->
                if (existingRoom == null) {
                    dispatch(Message.SetIsLoading(isLoading = false))
                    publish(Label.ShowError(kind = RoomEditorErrorKind.RoomNotFound))
                    return
                }
                val updated = existingRoom.copy(students = students)
                roomsRepository.saveRoom(updated)
                    .onSuccess {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowSuccess(kind = RoomEditorSuccessKind.StudentsSaved))
                        publish(Label.NavigateBack)
                    }
                    .onFailure {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowError(kind = RoomEditorErrorKind.FailedToSaveStudents))
                    }
            }
            .onFailure {
                dispatch(Message.SetIsLoading(isLoading = false))
                publish(Label.ShowError(kind = RoomEditorErrorKind.FailedToSaveStudents))
            }
    }

    private suspend fun saveDraft(currentRoom: Room, students: List<Student>) {
        roomsRepository.getDraftRoom()
            .onSuccess { draftRoom ->
                if (draftRoom == null) {
                    dispatch(Message.SetIsLoading(isLoading = false))
                    publish(Label.ShowError(kind = RoomEditorErrorKind.DraftRoomNotFound))
                    return
                }
                val updated = draftRoom.copy(students = students)
                roomsRepository.saveRoom(updated)
                    .onSuccess {
                        roomsRepository.clearDraftRoom()
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowSuccess(kind = RoomEditorSuccessKind.StudentsSaved))
                        publish(Label.NavigateBack)
                    }
                    .onFailure {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowError(kind = RoomEditorErrorKind.FailedToSaveStudents))
                    }
            }
            .onFailure {
                dispatch(Message.SetIsLoading(isLoading = false))
                publish(Label.ShowError(kind = RoomEditorErrorKind.FailedToSaveStudents))
            }
    }

    private fun digits(value: String): String =
        value.filter { it.isDigit() }.take(MAX_ROOM_INPUT_LENGTH)

    private fun updateRoomParam(transform: (State.RoomParams) -> State.RoomParams) {
        val current = state().roomParams ?: return
        dispatch(Message.SetRoomParams(transform(current)))
    }

    private fun handleEditRoomParamsClick() {
        val room = state().room ?: return
        dispatch(
            Message.SetRoomParams(
                State.RoomParams(
                    floorNumber = room.floorNumber.toString(),
                    roomNumber = room.roomNumber.toString(),
                    bedsCount = room.bedsCount.toString(),
                )
            )
        )
        dispatch(Message.SetShowRoomParams(show = true))
    }

    private suspend fun handleSaveRoomParams() {
        if (mode !is RoomEditorMode.ExistingRoom) return
        val room = state().room ?: return
        val params = state().roomParams ?: return

        val floor = params.floorNumber.toIntOrNull()
        val number = params.roomNumber.toIntOrNull()
        val beds = params.bedsCount.toIntOrNull()
        if (floor == null || number == null || beds == null || beds <= 0) {
            publish(Label.ShowError(kind = RoomEditorErrorKind.FailedToUpdateRoom))
            return
        }

        val existingRooms = roomsRepository.getAllRooms().getOrElse { emptyList() }
        if (RoomNumberConflict.exists(existingRooms, floor, number, excludeRoomId = room.id)) {
            dispatch(Message.SetRoomParams(params.copy(roomNumberError = true)))
            publish(Label.ShowError(kind = RoomEditorErrorKind.RoomNumberTaken))
            return
        }

        roomsRepository.updateRoomMetadata(
            roomId = room.id,
            floorNumber = floor,
            roomNumber = number,
            bedsCount = beds,
        ).onSuccess {
            dispatch(Message.SetRoom(room.copy(floorNumber = floor, roomNumber = number, bedsCount = beds)))
            dispatch(Message.SetShowRoomParams(show = false))
            publish(Label.ShowSuccess(kind = RoomEditorSuccessKind.RoomUpdated))
        }.onFailure {
            publish(Label.ShowError(kind = RoomEditorErrorKind.FailedToUpdateRoom))
        }
    }

    private suspend fun handleDeleteRoom() {
        if (mode !is RoomEditorMode.ExistingRoom) return
        val room = state().room ?: return
        roomsRepository.deleteRoom(roomId = room.id)
            .onSuccess {
                dispatch(Message.SetShowDeleteConfirm(show = false))
                publish(Label.ShowSuccess(kind = RoomEditorSuccessKind.RoomDeleted))
                publish(Label.NavigateBack)
            }
            .onFailure {
                dispatch(Message.SetShowDeleteConfirm(show = false))
                publish(Label.ShowError(kind = RoomEditorErrorKind.FailedToDeleteRoom))
            }
    }

    // Check-in/out are calendar dates without a time component. The Material date
    // picker emits and consumes UTC-midnight millis, so we canonicalize on UTC to
    // keep the displayed day equal to the picked day in every timezone.
    private fun formatDateFromMillis(millis: Long): String {
        val instant = Instant.fromEpochMilliseconds(millis)
        val localDate = instant.toLocalDateTime(TimeZone.UTC).date
        return dateDisplayFormat.format(localDate)
    }

    private fun parseDateToMillis(dateString: String): Long? = try {
        dateDisplayFormat.parse(dateString).toUtcMillis()
    } catch (_: Exception) {
        null
    }

    private fun LocalDate.toUtcMillis(): Long =
        atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

private const val MAX_ROOM_INPUT_LENGTH = 4
