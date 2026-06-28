package dev.nonoxy.residetrack.feature.rooms.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal actual fun rememberBackupFilePicker(
    onImported: (String) -> Unit,
    onExportCompleted: (Boolean) -> Unit,
): BackupFilePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // rememberSaveable so a destination chosen after process death still finds its JSON to write.
    var pendingJson by rememberSaveable { mutableStateOf<String?>(null) }

    val createLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val json = pendingJson
        pendingJson = null
        // Null uri means the user cancelled — stay silent. Only report once a destination was chosen.
        if (uri != null && json != null) {
            scope.launch {
                val written = withContext(Dispatchers.IO) {
                    runCatching {
                        val stream = context.contentResolver.openOutputStream(uri) ?: error("no output stream")
                        stream.use { it.write(json.encodeToByteArray()) }
                    }.isSuccess
                }
                onExportCompleted(written)
            }
        }
    }

    val openLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }.getOrNull()
                }
                if (json != null) onImported(json)
            }
        }
    }

    return remember {
        AndroidBackupFilePicker(
            launchCreate = { name, json ->
                pendingJson = json
                createLauncher.launch(name)
            },
            launchOpen = { openLauncher.launch(arrayOf("application/json")) },
            onExportCompleted = onExportCompleted,
            clearPending = { pendingJson = null },
        )
    }
}

private class AndroidBackupFilePicker(
    private val launchCreate: (name: String, json: String) -> Unit,
    private val launchOpen: () -> Unit,
    private val onExportCompleted: (Boolean) -> Unit,
    private val clearPending: () -> Unit,
) : BackupFilePicker {

    override fun launchSave(json: String, suggestedName: String) {
        // launch() throws ActivityNotFoundException when no document provider is available
        // (locked-down OEM / work profile). Report the failure instead of crashing.
        runCatching { launchCreate(suggestedName, json) }
            .onFailure {
                clearPending()
                onExportCompleted(false)
            }
    }

    override fun launchOpen() {
        // No completion signal on the open path — a missing provider just means no picker appears.
        runCatching { launchOpen() }
    }
}
