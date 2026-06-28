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
                Text(
                    modifier = Modifier.padding(top = padding_size_8),
                    text = stringResource(MR.strings.backup_import_dialog_counts, roomCount, studentCount),
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
