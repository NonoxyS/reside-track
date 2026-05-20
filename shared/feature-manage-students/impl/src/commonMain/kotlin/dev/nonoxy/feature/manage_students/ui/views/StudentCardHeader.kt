package dev.nonoxy.feature.manage_students.ui.views

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
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.size_24
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import residetrack.shared.feature_manage_students.impl.generated.resources.Res
import residetrack.shared.feature_manage_students.impl.generated.resources.ic_delete_circle
import residetrack.shared.feature_manage_students.impl.generated.resources.new_student
import residetrack.shared.feature_manage_students.impl.generated.resources.student_placeholder
import residetrack.shared.feature_manage_students.impl.generated.resources.student_title

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
                stringResource(Res.string.new_student)
            } else {
                val displayNumber = streamNumber.ifBlank {
                    stringResource(Res.string.student_placeholder)
                }
                stringResource(Res.string.student_title, displayNumber)
            },
            style = ResideTrackTheme.typography.lead,
            color = ResideTrackTheme.colors.textPrimary
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(size_24)
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_delete_circle),
                contentDescription = null,
                tint = ResideTrackTheme.colors.textError
            )
        }
    }
}
