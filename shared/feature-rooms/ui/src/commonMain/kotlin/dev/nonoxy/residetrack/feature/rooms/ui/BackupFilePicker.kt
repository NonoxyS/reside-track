package dev.nonoxy.residetrack.feature.rooms.ui

import androidx.compose.runtime.Composable

internal interface BackupFilePicker {

    fun launchSave(json: String, suggestedName: String)

    fun launchOpen()
}

@Composable
internal expect fun rememberBackupFilePicker(
    onImported: (String) -> Unit,
    onExportCompleted: (Boolean) -> Unit,
): BackupFilePicker
