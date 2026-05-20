package dev.nonoxy.feature.manage_students.presentation

import androidx.lifecycle.viewModelScope
import dev.nonoxy.common.presentation.BaseViewModel
import dev.nonoxy.common.utils.currentLocalDate
import dev.nonoxy.feature.manage_students.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.presentation.models.EditableStudent
import dev.nonoxy.feature.manage_students.presentation.models.ManageStudentsAction
import dev.nonoxy.feature.manage_students.presentation.models.ManageStudentsEvent
import dev.nonoxy.feature.manage_students.presentation.models.ManageStudentsViewState
import dev.nonoxy.feature.manage_students.ui.mappers.UiRoomMapper
import dev.nonoxy.feature.manage_students.ui.mappers.UiStudentMapper
import dev.nonoxy.feature.manage_students.utils.StringProvider
import dev.nonoxy.feature.rooms.models.Student
import dev.nonoxy.feature.rooms.repository.RoomsRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, FormatStringsInDatetimeFormats::class)
internal class ManageStudentsViewModel(
    private val mode: ManageStudentsMode,
    private val roomsRepository: RoomsRepository,
    private val uiRoomMapper: UiRoomMapper,
    private val uiStudentMapper: UiStudentMapper,
    private val stringProvider: StringProvider
) : BaseViewModel<ManageStudentsViewState, ManageStudentsEvent, ManageStudentsAction>(
    initialState = ManageStudentsViewState()
) {

    private val dateDisplayFormat = LocalDate.Format {
        byUnicodePattern("dd.MM.yyyy")
    }

    init {
        obtainEvent(ManageStudentsEvent.LoadStudents)
    }

    override fun obtainEvent(event: ManageStudentsEvent) {
        when (event) {
            ManageStudentsEvent.LoadStudents -> loadStudents()
            ManageStudentsEvent.OnAddStudent -> handleAddStudent()
            is ManageStudentsEvent.OnRemoveStudent -> handleRemoveStudent(event.studentId)
            is ManageStudentsEvent.OnStreamNumberChange -> handleStreamNumberChange(
                event.studentId,
                event.value
            )

            is ManageStudentsEvent.OnCheckInDateChange -> handleCheckInDateChange(
                event.studentId,
                event.value
            )

            is ManageStudentsEvent.OnCheckOutDateChange -> handleCheckOutDateChange(
                event.studentId,
                event.value
            )

            is ManageStudentsEvent.OnCheckInDateMillisChange -> handleCheckInDateMillisChange(
                event.studentId,
                event.millis
            )

            is ManageStudentsEvent.OnCheckOutDateMillisChange -> handleCheckOutDateMillisChange(
                event.studentId,
                event.millis
            )

            ManageStudentsEvent.OnSaveAndClose -> handleSaveAndClose()
            ManageStudentsEvent.OnClose -> handleClose()
        }
    }

    private fun formatDateFromMillis(millis: Long): String {
        val instant = Instant.fromEpochMilliseconds(millis)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return dateDisplayFormat.format(localDate)
    }

    private fun parseDateToMillis(dateString: String): Long? {
        return try {
            val localDate = dateDisplayFormat.parse(dateString)
            localDate.toEpochDays().seconds.inWholeSeconds
        } catch (e: Exception) {
            null
        }
    }

    private fun formatDateForStudent(student: Student): String {
        return dateDisplayFormat.format(student.checkInDate)
    }

    private fun loadStudents() {
        viewModelScope.launch {
            viewState = viewState.copy(isLoading = true, isError = false)

            try {
                when (mode) {
                    is ManageStudentsMode.ExistingRoom -> {
                        roomsRepository.getRoomById(mode.roomId.toLong())
                            .onSuccess { room ->
                                room?.let {
                                    val uiRoom = uiRoomMapper.map(it)
                                    val editableStudents = it.students.map { student ->
                                        val checkInDateString = dateDisplayFormat.format(student.checkInDate)
                                        val checkOutDateString = dateDisplayFormat.format(student.checkOutDate)

                                        EditableStudent(
                                            id = Uuid.random().toString(),
                                            studentId = student.id,
                                            streamNumber = student.streamNumber.toString(),
                                            checkInDate = checkInDateString,
                                            checkOutDate = checkOutDateString,
                                            checkInDateMillis = 1,
                                            checkOutDateMillis = 1,
                                            isNew = false
                                        )
                                    }.toImmutableList()

                                    viewState = viewState.copy(
                                        isLoading = false,
                                        room = uiRoom,
                                        editableStudents = editableStudents
                                    )
                                } ?: run {
                                    val errorMessage = stringProvider.getRoomNotFound()
                                    viewState = viewState.copy(
                                        isLoading = false,
                                        isError = true,
                                        errorMessage = errorMessage
                                    )
                                }
                            }
                            .onFailure { error ->
                                val errorMessage = stringProvider.getFailedToLoadStudents()
                                viewState = viewState.copy(
                                    isLoading = false,
                                    isError = true,
                                    errorMessage = errorMessage
                                )
                                viewAction = ManageStudentsAction.ShowError(errorMessage)
                            }
                    }

                    ManageStudentsMode.DraftRoom -> {
                        roomsRepository.getDraftRoom()
                            .onSuccess { room ->
                                room?.let {
                                    val uiRoom = uiRoomMapper.map(it)
                                    val editableStudents = it.students.map { student ->
                                        val checkInDateString = dateDisplayFormat.format(student.checkInDate)
                                        val checkOutDateString = dateDisplayFormat.format(student.checkOutDate)

                                        EditableStudent(
                                            id = Uuid.random().toString(),
                                            studentId = student.id,
                                            streamNumber = student.streamNumber.toString(),
                                            checkInDate = checkInDateString,
                                            checkOutDate = checkOutDateString,
                                            checkInDateMillis = 1,
                                            checkOutDateMillis = 1,
                                            isNew = false
                                        )
                                    }.toImmutableList()

                                    viewState = viewState.copy(
                                        isLoading = false,
                                        room = uiRoom,
                                        editableStudents = editableStudents
                                    )
                                } ?: run {
                                    val errorMessage = stringProvider.getDraftRoomNotFound()
                                    viewState = viewState.copy(
                                        isLoading = false,
                                        isError = true,
                                        errorMessage = errorMessage
                                    )
                                }
                            }
                            .onFailure { error ->
                                val errorMessage = stringProvider.getFailedToLoadStudents()
                                viewState = viewState.copy(
                                    isLoading = false,
                                    isError = true,
                                    errorMessage = errorMessage
                                )
                                viewAction = ManageStudentsAction.ShowError(errorMessage)
                            }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = stringProvider.getFailedToLoadStudents()
                viewState = viewState.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = errorMessage
                )
                viewAction = ManageStudentsAction.ShowError(errorMessage)
            }
        }
    }

    private fun handleAddStudent() {
        val newStudent = EditableStudent(
            id = Uuid.random().toString(),
            studentId = null,
            streamNumber = "",
            checkInDate = "",
            checkOutDate = "",
            checkInDateMillis = null,
            checkOutDateMillis = null,
            isNew = true
        )

        val updatedStudents = (viewState.editableStudents + newStudent).toImmutableList()
        viewState = viewState.copy(editableStudents = updatedStudents)
    }

    private fun handleRemoveStudent(studentId: String) {
        val updatedStudents = viewState.editableStudents.toMutableList().apply {
            removeAll { it.id == studentId }
        }.toImmutableList()

        viewState = viewState.copy(editableStudents = updatedStudents)
    }

    private fun handleStreamNumberChange(studentId: String, value: String) {
        val filteredValue = value.filter { it.isDigit() }

        val updatedStudents = viewState.editableStudents.map { student ->
            if (student.id == studentId) {
                student.copy(streamNumber = filteredValue)
            } else {
                student
            }
        }.toImmutableList()

        viewState = viewState.copy(editableStudents = updatedStudents)
    }

    private fun handleCheckInDateChange(studentId: String, value: String) {
        val updatedStudents = viewState.editableStudents.map { student ->
            if (student.id == studentId) {
                val millis = parseDateToMillis(value)
                student.copy(
                    checkInDate = value,
                    checkInDateMillis = millis
                )
            } else {
                student
            }
        }.toImmutableList()

        viewState = viewState.copy(editableStudents = updatedStudents)
    }

    private fun handleCheckOutDateChange(studentId: String, value: String) {
        val updatedStudents = viewState.editableStudents.map { student ->
            if (student.id == studentId) {
                val millis = parseDateToMillis(value)
                student.copy(
                    checkOutDate = value,
                    checkOutDateMillis = millis
                )
            } else {
                student
            }
        }.toImmutableList()

        viewState = viewState.copy(editableStudents = updatedStudents)
    }

    private fun handleCheckInDateMillisChange(studentId: String, millis: Long) {
        val dateString = formatDateFromMillis(millis)
        val updatedStudents = viewState.editableStudents.map { student ->
            if (student.id == studentId) {
                student.copy(
                    checkInDate = dateString,
                    checkInDateMillis = millis
                )
            } else {
                student
            }
        }.toImmutableList()

        viewState = viewState.copy(editableStudents = updatedStudents)
    }

    private fun handleCheckOutDateMillisChange(studentId: String, millis: Long) {
        val dateString = formatDateFromMillis(millis)
        val updatedStudents = viewState.editableStudents.map { student ->
            if (student.id == studentId) {
                student.copy(
                    checkOutDate = dateString,
                    checkOutDateMillis = millis
                )
            } else {
                student
            }
        }.toImmutableList()

        viewState = viewState.copy(editableStudents = updatedStudents)
    }

    private fun handleSaveAndClose() {
        viewModelScope.launch {
            viewState = viewState.copy(isLoading = true)

            try {
                val room = viewState.room
                if (room == null) {
                    val errorMessage = stringProvider.getRoomNotFound()
                    viewState = viewState.copy(isLoading = false)
                    viewAction = ManageStudentsAction.ShowError(errorMessage)
                    return@launch
                }

                val students = viewState.editableStudents.mapNotNull { editableStudent ->
                    try {
                        if (editableStudent.streamNumber.isBlank() ||
                            editableStudent.checkInDate.isBlank() ||
                            editableStudent.checkOutDate.isBlank()
                        ) {
                            null
                        } else {
                            val streamNumber = editableStudent.streamNumber.toIntOrNull()
                            if (streamNumber == null) {
                                val errorMessage = stringProvider.getStreamNumberInvalid()
                                viewState = viewState.copy(isLoading = false)
                                viewAction = ManageStudentsAction.ShowError(errorMessage)
                                return@launch
                            }

                            val checkInDate = dateDisplayFormat.parse(editableStudent.checkInDate)
                            val checkOutDate = dateDisplayFormat.parse(editableStudent.checkOutDate)

                            if (checkOutDate <= checkInDate) {
                                val errorMessage = stringProvider.getInvalidDateRange()
                                viewState = viewState.copy(isLoading = false)
                                viewAction = ManageStudentsAction.ShowError(errorMessage)
                                return@launch
                            }

                            val currentDate = currentLocalDate
                            val isCheckOutDateNearOrExpired =
                                checkOutDate.minus(currentDate).days <= 3

                            Student(
                                id = editableStudent.studentId ?: 0L,
                                streamNumber = streamNumber,
                                checkInDate = checkInDate,
                                checkOutDate = checkOutDate,
                                isCheckOutDateNearOrExpired = isCheckOutDateNearOrExpired
                            )
                        }
                    } catch (e: Exception) {
                        val errorMessage = stringProvider.getInvalidDateFormat()
                        viewState = viewState.copy(isLoading = false)
                        viewAction = ManageStudentsAction.ShowError(errorMessage)
                        return@launch
                    }
                }

                val streamNumbers = students.map { it.streamNumber }
                if (streamNumbers.size != streamNumbers.distinct().size) {
                    val errorMessage = stringProvider.getDuplicateStreamNumbers()
                    viewState = viewState.copy(isLoading = false)
                    viewAction = ManageStudentsAction.ShowError(errorMessage)
                    return@launch
                }

                when (mode) {
                    is ManageStudentsMode.ExistingRoom -> {
                        roomsRepository.getRoomById(mode.roomId.toLong())
                            .onSuccess { existingRoom ->
                                existingRoom?.let { room ->
                                    val updatedRoom = room.copy(students = students)
                                    roomsRepository.saveRoom(updatedRoom)
                                        .onSuccess {
                                            val successMessage =
                                                stringProvider.getStudentsSavedSuccessfully()
                                            viewState = viewState.copy(isLoading = false)
                                            viewAction =
                                                ManageStudentsAction.ShowSuccess(successMessage)
                                            viewAction = ManageStudentsAction.NavigateBack
                                        }
                                        .onFailure { error ->
                                            val errorMessage =
                                                stringProvider.getFailedToSaveStudents()
                                            viewState = viewState.copy(isLoading = false)
                                            viewAction =
                                                ManageStudentsAction.ShowError(errorMessage)
                                        }
                                } ?: run {
                                    val errorMessage = stringProvider.getRoomNotFound()
                                    viewState = viewState.copy(isLoading = false)
                                    viewAction = ManageStudentsAction.ShowError(errorMessage)
                                }
                            }
                            .onFailure { error ->
                                val errorMessage = stringProvider.getFailedToSaveStudents()
                                viewState = viewState.copy(isLoading = false)
                                viewAction = ManageStudentsAction.ShowError(errorMessage)
                            }
                    }

                    ManageStudentsMode.DraftRoom -> {
                        roomsRepository.getDraftRoom()
                            .onSuccess { draftRoom ->
                                draftRoom?.let { room ->
                                    val updatedRoom = room.copy(students = students)
                                    roomsRepository.saveDraftRoom(updatedRoom)
                                        .onSuccess {
                                            val successMessage =
                                                stringProvider.getStudentsSavedSuccessfully()
                                            viewState = viewState.copy(isLoading = false)
                                            viewAction =
                                                ManageStudentsAction.ShowSuccess(successMessage)
                                            viewAction = ManageStudentsAction.NavigateBack
                                        }
                                        .onFailure { error ->
                                            val errorMessage =
                                                stringProvider.getFailedToSaveStudents()
                                            viewState = viewState.copy(isLoading = false)
                                            viewAction =
                                                ManageStudentsAction.ShowError(errorMessage)
                                        }
                                } ?: run {
                                    val errorMessage = stringProvider.getDraftRoomNotFound()
                                    viewState = viewState.copy(isLoading = false)
                                    viewAction = ManageStudentsAction.ShowError(errorMessage)
                                }
                            }
                            .onFailure { error ->
                                val errorMessage = stringProvider.getFailedToSaveStudents()
                                viewState = viewState.copy(isLoading = false)
                                viewAction = ManageStudentsAction.ShowError(errorMessage)
                            }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = stringProvider.getFailedToSaveStudents()
                viewState = viewState.copy(isLoading = false)
                viewAction = ManageStudentsAction.ShowError(errorMessage)
            }
        }
    }

    private fun handleClose() {
        viewAction = ManageStudentsAction.NavigateBack
    }

    private fun generateUuid(): String = Uuid.random().toString()
}
