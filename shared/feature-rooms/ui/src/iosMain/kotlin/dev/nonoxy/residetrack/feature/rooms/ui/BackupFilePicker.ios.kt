package dev.nonoxy.residetrack.feature.rooms.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.darwin.NSObject

@Composable
internal actual fun rememberBackupFilePicker(
    onImported: (String) -> Unit,
    onExportCompleted: (Boolean) -> Unit,
): BackupFilePicker = remember { IosBackupFilePicker(onImported, onExportCompleted) }

@OptIn(ExperimentalForeignApi::class)
private class IosBackupFilePicker(
    private val onImported: (String) -> Unit,
    private val onExportCompleted: (Boolean) -> Unit,
) : BackupFilePicker {

    // Strong ref so the delegate survives while the picker is on screen.
    private var delegate: PickerDelegate? = null

    override fun launchSave(json: String, suggestedName: String) {
        val path = (NSTemporaryDirectory() as NSString).stringByAppendingPathComponent(suggestedName)
        val url = NSURL.fileURLWithPath(path)
        val written = (json as NSString).writeToURL(
            url,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )
        if (!written) {
            onExportCompleted(false)
            return
        }
        val picker = UIDocumentPickerViewController(forExportingURLs = listOf(url))
        present(picker, onPicked = { onExportCompleted(true) }, onCancelled = {})
    }

    override fun launchOpen() {
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeJSON))
        present(
            picker,
            onPicked = { urls ->
                val url = urls.firstOrNull() as? NSURL ?: return@present
                val accessed = url.startAccessingSecurityScopedResource()
                val content = NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null)
                if (accessed) url.stopAccessingSecurityScopedResource()
                if (content != null) onImported(content)
            },
            onCancelled = {},
        )
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
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        root?.presentViewController(picker, animated = true, completion = null)
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
