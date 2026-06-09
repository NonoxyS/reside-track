package dev.nonoxy.residetrack.common.ui.common.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR

/**
 * Confirmation shown when the user tries to leave a screen with unsaved changes.
 *
 * @param onConfirm discard the changes and proceed with leaving.
 * @param onDismiss keep editing — stay on the screen.
 */
@Composable
fun DiscardChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ResideTrackTheme.colors.surface,
        titleContentColor = ResideTrackTheme.colors.textPrimary,
        textContentColor = ResideTrackTheme.colors.textBody,
        title = {
            Text(text = stringResource(MR.strings.discard_changes_title))
        },
        text = {
            Text(text = stringResource(MR.strings.discard_changes_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(MR.strings.discard_changes_confirm),
                    color = ResideTrackTheme.colors.textError
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(MR.strings.keep_editing),
                    color = ResideTrackTheme.colors.textPrimary
                )
            }
        }
    )
}
