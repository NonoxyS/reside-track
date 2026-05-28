package dev.nonoxy.residetrack.feature.manage_students.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.size_24
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

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(size_24)
        ) {
            Icon(
                painter = painterResource(MR.images.ic_delete_circle),
                contentDescription = null,
                tint = ResideTrackTheme.colors.textError
            )
        }
    }
}
