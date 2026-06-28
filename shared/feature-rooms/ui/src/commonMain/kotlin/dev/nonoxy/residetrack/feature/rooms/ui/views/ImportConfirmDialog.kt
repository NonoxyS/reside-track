package dev.nonoxy.residetrack.feature.rooms.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_8
import dev.nonoxy.residetrack.res.MR

@Composable
internal fun ImportConfirmDialog(
    roomCount: Int,
    studentCount: Int,
    currentRoomCount: Int,
    currentStudentCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ResideTrackTheme.colors.surface,
        titleContentColor = ResideTrackTheme.colors.textPrimary,
        textContentColor = ResideTrackTheme.colors.textBody,
        title = {
            Text(text = stringResource(MR.strings.backup_import_dialog_title))
        },
        text = {
            Column {
                Text(text = stringResource(MR.strings.backup_import_dialog_message))
                // "Сейчас" in error colour makes the data about to be destroyed the loud part;
                // "Станет" shows what replaces it, so the magnitude of the loss is explicit.
                Text(
                    modifier = Modifier.padding(top = padding_size_8),
                    text = stringResource(
                        MR.strings.backup_import_current,
                        stringResource(MR.plurals.backup_rooms_count, currentRoomCount, currentRoomCount),
                        stringResource(MR.plurals.backup_students_count, currentStudentCount, currentStudentCount),
                    ),
                    color = ResideTrackTheme.colors.textError,
                )
                Text(
                    text = stringResource(
                        MR.strings.backup_import_after,
                        stringResource(MR.plurals.backup_rooms_count, roomCount, roomCount),
                        stringResource(MR.plurals.backup_students_count, studentCount, studentCount),
                    ),
                    color = ResideTrackTheme.colors.textCaption,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(MR.strings.backup_import_confirm),
                    color = ResideTrackTheme.colors.textError,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(MR.strings.backup_cancel),
                    color = ResideTrackTheme.colors.textPrimary,
                )
            }
        },
    )
}
