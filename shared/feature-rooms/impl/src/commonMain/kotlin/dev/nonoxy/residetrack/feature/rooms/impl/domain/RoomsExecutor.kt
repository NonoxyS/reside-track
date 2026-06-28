package dev.nonoxy.residetrack.feature.rooms.impl.domain

import dev.nonoxy.residetrack.common.utils.currentLocalDate
import dev.nonoxy.residetrack.core.backup.domain.repository.BackupRepository
import dev.nonoxy.residetrack.core.mvikotlin.BaseExecutor
import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.repository.RoomsRepository
import dev.nonoxy.residetrack.feature.rooms.api.store.BackupErrorKind
import dev.nonoxy.residetrack.feature.rooms.api.store.BackupSuccessKind
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.State
import dev.nonoxy.residetrack.feature.rooms.impl.domain.RoomsStoreFactory.Action
import dev.nonoxy.residetrack.feature.rooms.impl.domain.RoomsStoreFactory.Message
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch

internal class RoomsExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
    private val backupRepository: BackupRepository,
) : BaseExecutor<Intent, Action, State, Message, Label>(mainContext = mainDispatcher) {

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.LoadInitial -> loadRoomsData()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnRoomClick -> publish(
                Label.NavigateToRoomEditorExistingRoom(roomId = intent.roomId.toString())
            )

            Intent.OnAddRoomClick -> publish(Label.NavigateToAddRoomScreen)
            Intent.OnRetry -> loadRoomsData()

            Intent.OnExportClick -> exportBackup()
            is Intent.OnExportCompleted -> publish(
                if (intent.success) {
                    Label.ShowBackupSuccess(BackupSuccessKind.Exported)
                } else {
                    Label.ShowBackupError(BackupErrorKind.ExportFailed)
                }
            )

            Intent.OnImportClick -> publish(Label.OpenBackupFile)
            is Intent.OnBackupFileLoaded -> parseBackup(intent.json)
            Intent.OnRestoreConfirm -> restoreBackup()
            Intent.OnRestoreCancel -> dispatch(Message.SetImportConfirmation(backup = null))
        }
    }

    private suspend fun exportBackup() {
        // Nothing to back up yet — refuse instead of writing a misleading "empty backup" file
        // and reporting success. roomsOnFloor mirrors the DB contents already loaded into state.
        if (state().roomsOnFloor.isEmpty()) {
            publish(Label.ShowBackupError(BackupErrorKind.ExportNoData))
            return
        }
        backupRepository.export()
            .onSuccess { json ->
                publish(
                    Label.SaveBackupFile(
                        json = json,
                        suggestedName = "reside-track-backup-$currentLocalDate.json",
                    )
                )
            }
            .onFailure { publish(Label.ShowBackupError(BackupErrorKind.ExportFailed)) }
    }

    private suspend fun parseBackup(json: String) {
        backupRepository.parse(json)
            .onSuccess { backup ->
                // A backup with no rooms would only wipe existing data on restore — block it
                // rather than letting the user confirm a destructive no-op.
                if (backup.isEmpty) {
                    publish(Label.ShowBackupError(BackupErrorKind.ImportEmpty))
                } else {
                    dispatch(Message.SetImportConfirmation(backup = backup))
                }
            }
            .onFailure { publish(Label.ShowBackupError(BackupErrorKind.ImportReadFailed)) }
    }

    private suspend fun restoreBackup() {
        val backup = state().importConfirmation ?: return
        dispatch(Message.SetImportConfirmation(backup = null))
        backupRepository.restore(backup)
            .onSuccess { publish(Label.ShowBackupSuccess(BackupSuccessKind.Restored)) }
            .onFailure { publish(Label.ShowBackupError(BackupErrorKind.RestoreFailed)) }
    }

    private suspend fun loadRoomsData() {
        dispatch(Message.SetIsLoading(isLoading = true))
        roomsRepository.observeRooms()
            .catch { dispatch(Message.SetError) }
            .collect { rooms ->
                // DB query returns rooms unordered (no ORDER BY); sort here so floors
                // page in ascending order and rooms read top-down within a floor.
                val grouped = rooms
                    .sortedWith(compareBy(Room::floorNumber, Room::roomNumber))
                    .groupBy { room -> room.floorNumber }
                dispatch(Message.SetRoomsOnFloor(roomsOnFloor = grouped))
            }
    }
}
