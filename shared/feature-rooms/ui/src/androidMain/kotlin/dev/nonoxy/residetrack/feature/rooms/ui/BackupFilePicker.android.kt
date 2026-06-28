package dev.nonoxy.residetrack.feature.rooms.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberBackupFilePicker(
    onImported: (String) -> Unit,
    onExportCompleted: (Boolean) -> Unit,
): BackupFilePicker {
    val context = LocalContext.current
    var pendingJson by remember { mutableStateOf<String?>(null) }

    val createLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val json = pendingJson
        pendingJson = null
        // Null uri means the user cancelled — stay silent. Only report once a destination was chosen.
        if (uri != null && json != null) {
            val written = runCatching {
                val stream = context.contentResolver.openOutputStream(uri) ?: error("no output stream")
                stream.use { it.write(json.encodeToByteArray()) }
            }.isSuccess
            onExportCompleted(written)
        }
    }

    val openLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val json = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            if (json != null) onImported(json)
        }
    }

    return remember {
        object : BackupFilePicker {
            override fun launchSave(json: String, suggestedName: String) {
                pendingJson = json
                createLauncher.launch(suggestedName)
            }

            override fun launchOpen() {
                openLauncher.launch(arrayOf("application/json"))
            }
        }
    }
}
