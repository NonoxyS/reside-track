package dev.nonoxy.residetrack.feature.rooms.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.darwin.NSObject

@Composable
internal actual fun rememberBackupFilePicker(
    onImported: (String) -> Unit,
    onExportCompleted: (Boolean) -> Unit,
): BackupFilePicker {
    val scope = rememberCoroutineScope()
    val currentOnImported by rememberUpdatedState(onImported)
    val currentOnExportCompleted by rememberUpdatedState(onExportCompleted)

    return remember {
        IosBackupFilePicker(
            scope = scope,
            onImported = { json -> currentOnImported(json) },
            onExportCompleted = { isSuccess -> currentOnExportCompleted(isSuccess) },
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosBackupFilePicker(
    private val scope: CoroutineScope,
    private val onImported: (String) -> Unit,
    private val onExportCompleted: (Boolean) -> Unit,
) : BackupFilePicker {

    // Strong ref so the delegate survives while the picker is on screen.
    private var delegate: PickerDelegate? = null

    override fun launchSave(json: String, suggestedName: String) {
        scope.launch {
            val tempUrl = withContext(Dispatchers.IO) { writeTempFile(json, suggestedName) }
            if (tempUrl == null) {
                onExportCompleted(false)
                return@launch
            }
            // Resumed on the main dispatcher — UIKit presentation must stay on the main thread.
            val picker = UIDocumentPickerViewController(forExportingURLs = listOf(tempUrl))
            present(
                picker = picker,
                onPicked = { urls ->
                    scope.launch {
                        val saved = withContext(Dispatchers.IO) {
                            val exists = (urls.firstOrNull() as? NSURL)?.let(::destinationExists) ?: false
                            removeTempFile(tempUrl)
                            exists
                        }
                        onExportCompleted(saved)
                    }
                },
                onCancelled = {
                    scope.launch { withContext(Dispatchers.IO) { removeTempFile(tempUrl) } }
                },
            )
        }
    }

    override fun launchOpen() {
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeJSON))
        present(
            picker,
            onPicked = { urls ->
                val url = urls.firstOrNull() as? NSURL ?: return@present
                scope.launch {
                    val content = withContext(Dispatchers.IO) { readSecurityScoped(url) }
                    if (content != null) onImported(content)
                }
            },
            onCancelled = {},
        )
    }

    private fun writeTempFile(json: String, name: String): NSURL? {
        val dir = NSURL.fileURLWithPath(NSTemporaryDirectory(), isDirectory = true)
        val url = dir.URLByAppendingPathComponent(name) ?: return null
        val bytes = json.encodeToByteArray()
        if (bytes.isEmpty()) return null
        val written = bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong(),
            ).writeToURL(url, atomically = true)
        }
        return if (written) url else null
    }

    private fun removeTempFile(url: NSURL) {
        NSFileManager.defaultManager.removeItemAtURL(url, error = null)
    }

    /** Confirms the export landed: `forExportingURLs` reports the destination URL after the copy. */
    private fun destinationExists(url: NSURL): Boolean {
        val accessed = url.startAccessingSecurityScopedResource()
        return try {
            val path = url.path
            path != null && NSFileManager.defaultManager.fileExistsAtPath(path)
        } finally {
            if (accessed) url.stopAccessingSecurityScopedResource()
        }
    }

    private fun readSecurityScoped(url: NSURL): String? {
        val accessed = url.startAccessingSecurityScopedResource()
        return try {
            NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, error = null)
        } finally {
            if (accessed) url.stopAccessingSecurityScopedResource()
        }
    }

    private fun present(
        picker: UIDocumentPickerViewController,
        onPicked: (List<*>) -> Unit,
        onCancelled: () -> Unit,
    ) {
        val pickerDelegate = PickerDelegate(
            onPickedUrls = { urls ->
                delegate = null
                onPicked(urls)
            },
            onCancelledPick = {
                delegate = null
                onCancelled()
            },
        )
        delegate = pickerDelegate
        picker.delegate = pickerDelegate
        topmostViewController()?.presentViewController(
            viewControllerToPresent = picker,
            animated = true,
            completion = null,
        )
    }

    // Compose Multiplatform creates a separate UIWindow for Popup/DropdownMenu with a higher
    // windowLevel, making it the keyWindow while a dropdown is visible. Presenting from that
    // popup window silently fails, so we always pick the main app window (lowest windowLevel)
    // and then traverse the presentedViewController chain.
    private fun topmostViewController(): UIViewController? {
        val scenes = UIApplication.sharedApplication.connectedScenes
        val activeScene = scenes
            .mapNotNull { scene -> scene as? UIWindowScene }
            .firstOrNull { scene -> scene.activationState == UISceneActivationStateForegroundActive }
            ?: scenes.firstNotNullOfOrNull { scene -> scene as? UIWindowScene }

        val windows = activeScene?.windows?.mapNotNull { it as? UIWindow } ?: return null
        val mainWindow = windows.minByOrNull { it.windowLevel }
        var vc = mainWindow?.rootViewController
        while (vc?.presentedViewController != null) {
            vc = vc.presentedViewController
        }
        return vc
    }
}

private class PickerDelegate(
    private val onPickedUrls: (List<*>) -> Unit,
    private val onCancelledPick: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        onPickedUrls(didPickDocumentsAtURLs)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onCancelledPick()
    }
}
