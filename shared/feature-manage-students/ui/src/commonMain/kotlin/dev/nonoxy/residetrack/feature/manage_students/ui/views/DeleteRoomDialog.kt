package dev.nonoxy.residetrack.feature.manage_students.ui.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.res.MR

@Composable
internal fun DeleteRoomDialog(
    roomNumber: String,
    studentCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ResideTrackTheme.colors.surface,
        titleContentColor = ResideTrackTheme.colors.textPrimary,
        textContentColor = ResideTrackTheme.colors.textBody,
        title = {
            Text(text = stringResource(MR.strings.room_editor_delete_confirm_title, roomNumber))
        },
        text = {
            Text(text = stringResource(MR.strings.room_editor_delete_confirm_message, studentCount))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(MR.strings.delete),
                    color = ResideTrackTheme.colors.textError,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(MR.strings.cancel),
                    color = ResideTrackTheme.colors.textPrimary,
                )
            }
        },
    )
}
