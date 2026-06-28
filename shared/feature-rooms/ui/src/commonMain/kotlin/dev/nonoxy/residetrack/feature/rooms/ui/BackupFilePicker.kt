package dev.nonoxy.residetrack.feature.rooms.ui

import androidx.compose.runtime.Composable

/**
 * Thin platform conduit for the system file picker. Holds zero domain logic: it only launches the
 * picker and streams bytes ↔ String. Everything else (serialize, validate, persist) lives in
 * core-backup; the screen wires these callbacks to the view-model.
 */
internal interface BackupFilePicker {
    /** Lets the user pick a destination and writes [json], then reports success via onExportCompleted. */
    fun launchSave(json: String, suggestedName: String)

    /** Lets the user pick a backup file; its contents are delivered via onImported. */
    fun launchOpen()
}

@Composable
internal expect fun rememberBackupFilePicker(
    onImported: (String) -> Unit,
    onExportCompleted: (Boolean) -> Unit,
): BackupFilePicker
