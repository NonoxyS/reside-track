package dev.nonoxy.residetrack.feature.rooms.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.State

interface RoomsStore : Store<Intent, State, Label> {

    data class State(
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val roomsOnFloor: Map<Int, List<Room>> = emptyMap(),
        /** Non-null while the import confirmation dialog is shown — the parsed, not-yet-applied backup. */
        val importConfirmation: Backup? = null,
    )

    sealed interface Intent {
        data class OnRoomClick(val roomId: Long) : Intent
        data object OnAddRoomClick : Intent
        data object OnRetry : Intent

        data object OnExportClick : Intent

        /** Reported by the UI after writing (or failing to write) the exported file to a picked location. */
        data class OnExportCompleted(val success: Boolean) : Intent
        data object OnImportClick : Intent

        /** Reported by the UI with the contents of a picked backup file. */
        data class OnBackupFileLoaded(val json: String) : Intent
        data object OnRestoreConfirm : Intent
        data object OnRestoreCancel : Intent
    }

    sealed interface Label {
        data object NavigateToAddRoomScreen : Label
        data class NavigateToRoomEditorExistingRoom(val roomId: String) : Label

        /** Asks the UI to write [json] to a user-picked file, defaulting its name to [suggestedName]. */
        data class SaveBackupFile(val json: String, val suggestedName: String) : Label

        /** Asks the UI to let the user pick a backup file to import. */
        data object OpenBackupFile : Label
        data class ShowBackupSuccess(val kind: BackupSuccessKind) : Label
        data class ShowBackupError(val kind: BackupErrorKind) : Label
    }
}
