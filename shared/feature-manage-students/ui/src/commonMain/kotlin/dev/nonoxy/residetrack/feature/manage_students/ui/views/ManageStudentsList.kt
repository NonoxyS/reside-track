package dev.nonoxy.residetrack.feature.manage_students.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiEditableStudent
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_12
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import dev.nonoxy.residetrack.common.ui.theme.padding_size_8
import kotlinx.collections.immutable.ImmutableList
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR

@Composable
internal fun ManageStudentsList(
    students: ImmutableList<UiEditableStudent>,
    onAddStudent: () -> Unit,
    onRemoveStudent: (String) -> Unit,
    onStreamNumberChange: (String, String) -> Unit,
    onCheckInDateChange: (String, String) -> Unit,
    onCheckOutDateChange: (String, String) -> Unit,
    onCheckInDateMillisChange: (String, Long) -> Unit,
    onCheckOutDateMillisChange: (String, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(padding_size_16),
        verticalArrangement = Arrangement.spacedBy(padding_size_12),
    ) {
        if (students.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(
                        MR.strings.students_total_count,
                        students.size,
                    ),
                    style = ResideTrackTheme.typography.paragraph,
                    color = ResideTrackTheme.colors.textCaption,
                )
                Spacer(modifier = Modifier.height(padding_size_8))
            }
        }

        items(
            items = students,
            key = { student -> student.id },
        ) { student ->
            EditableStudentCard(
                modifier = Modifier.fillMaxWidth().animateItem(),
                student = student,
                onStreamNumberChange = { value ->
                    onStreamNumberChange(student.id, value)
                },
                onCheckInDateMillisChange = { value ->
                    onCheckInDateMillisChange(student.id, value)
                },
                onCheckOutDateMillisChange = { value ->
                    onCheckOutDateMillisChange(student.id, value)
                },
                onRemove = {
                    onRemoveStudent(student.id)
                },
            )
        }

        item {
            OutlinedButton(
                onClick = onAddStudent,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(
                    width = 1.dp,
                    color = ResideTrackTheme.colors.borderDefault,
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ResideTrackTheme.colors.textPrimary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(padding_size_8))
                Text(stringResource(MR.strings.add_student))
            }
        }

        if (students.isEmpty()) {
            item {
                EmptyStudentsState()
            }
        }
    }
}
