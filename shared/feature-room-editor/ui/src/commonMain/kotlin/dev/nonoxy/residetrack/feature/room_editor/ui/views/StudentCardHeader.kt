package dev.nonoxy.residetrack.feature.room_editor.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(MR.images.ic_delete_circle),
                contentDescription = stringResource(MR.strings.remove_student),
                tint = ResideTrackTheme.colors.textError
            )
        }
    }
}
