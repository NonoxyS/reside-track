package dev.nonoxy.residetrack.feature.room_editor.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR

@Composable
internal fun StudentCardHeader(
    streamNumber: String,
    isNew: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRemoveConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isNew) {
                stringResource(MR.strings.new_student)
            } else {
                val displayNumber = streamNumber.ifBlank {
                    stringResource(MR.strings.student_placeholder)
                }
                stringResource(MR.strings.student_title, displayNumber)
            },
            style = ResideTrackTheme.typography.lead,
            color = ResideTrackTheme.colors.textPrimary
        )

        IconButton(onClick = { showRemoveConfirm = true }) {
            Icon(
                painter = painterResource(MR.images.ic_delete_circle),
                contentDescription = stringResource(MR.strings.remove_student),
                tint = ResideTrackTheme.colors.textError
            )
        }
    }

    if (showRemoveConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
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
                TextButton(
                    onClick = {
                        showRemoveConfirm = false
                        onRemove()
                    }
                ) {
                    Text(
                        text = stringResource(MR.strings.delete),
                        color = ResideTrackTheme.colors.textError
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirm = false }) {
                    Text(
                        text = stringResource(MR.strings.cancel),
                        color = ResideTrackTheme.colors.textPrimary
                    )
                }
            }
        )
    }
}
