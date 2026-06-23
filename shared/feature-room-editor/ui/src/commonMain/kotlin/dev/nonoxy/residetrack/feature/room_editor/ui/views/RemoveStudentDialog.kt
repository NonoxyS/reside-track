package dev.nonoxy.residetrack.feature.room_editor.ui.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.res.MR

@Composable
internal fun RemoveStudentDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ResideTrackTheme.colors.surface,
        titleContentColor = ResideTrackTheme.colors.textPrimary,
        textContentColor = ResideTrackTheme.colors.textBody,
        title = {
            Text(text = stringResource(MR.strings.remove_student_confirm_title))
        },
        text = {
            Text(text = stringResource(MR.strings.remove_student_confirm_message))
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
