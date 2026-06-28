package dev.nonoxy.residetrack.feature.rooms.presentation.mappers

import dev.nonoxy.residetrack.common.resources.StringConverter
import dev.nonoxy.residetrack.feature.rooms.api.store.BackupErrorKind
import dev.nonoxy.residetrack.feature.rooms.api.store.BackupSuccessKind
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiRoomsLabel
import dev.nonoxy.residetrack.res.MR

internal interface UiRoomsLabelMapper {
    fun map(item: RoomsStore.Label): UiRoomsLabel
}

internal class UiRoomsLabelMapperImpl(
    private val stringConverter: StringConverter,
) : UiRoomsLabelMapper {

    override fun map(item: RoomsStore.Label): UiRoomsLabel = when (item) {
        RoomsStore.Label.NavigateToAddRoomScreen ->
            UiRoomsLabel.NavigateToAddRoomScreen

        is RoomsStore.Label.NavigateToRoomEditorExistingRoom ->
            UiRoomsLabel.NavigateToRoomEditorExistingRoom(roomId = item.roomId)

        is RoomsStore.Label.SaveBackupFile ->
            UiRoomsLabel.SaveBackupFile(json = item.json, suggestedName = item.suggestedName)

        RoomsStore.Label.OpenBackupFile ->
            UiRoomsLabel.OpenBackupFile

        is RoomsStore.Label.ShowBackupSuccess ->
            UiRoomsLabel.ShowBackupSuccess(message = item.kind.toMessage())

        is RoomsStore.Label.ShowBackupError ->
            UiRoomsLabel.ShowBackupError(message = item.kind.toMessage())
    }

    private fun BackupSuccessKind.toMessage(): String = when (this) {
        BackupSuccessKind.Exported -> stringConverter.convert(MR.strings.backup_export_success)
        BackupSuccessKind.Restored -> stringConverter.convert(MR.strings.backup_import_success)
    }

    private fun BackupErrorKind.toMessage(): String = when (this) {
        BackupErrorKind.ExportNoData -> stringConverter.convert(MR.strings.backup_export_empty)
        BackupErrorKind.ExportFailed -> stringConverter.convert(MR.strings.backup_export_error)
        BackupErrorKind.ImportReadFailed -> stringConverter.convert(MR.strings.backup_import_error)
        BackupErrorKind.ImportVersionUnsupported ->
            stringConverter.convert(MR.strings.backup_import_error_version)
        BackupErrorKind.ImportEmpty -> stringConverter.convert(MR.strings.backup_import_empty)
        BackupErrorKind.RestoreFailed -> stringConverter.convert(MR.strings.backup_restore_error)
    }
}
