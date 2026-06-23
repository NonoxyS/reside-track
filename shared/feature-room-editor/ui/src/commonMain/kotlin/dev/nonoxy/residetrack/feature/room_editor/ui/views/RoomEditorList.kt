package dev.nonoxy.residetrack.feature.room_editor.ui.views

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
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiDateField
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiEditableStudent
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiOpenDatePicker
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_12
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import dev.nonoxy.residetrack.common.ui.theme.padding_size_8
import dev.nonoxy.residetrack.common.ui.theme.padding_size_96
import dev.nonoxy.residetrack.common.ui.theme.size_1
import kotlinx.collections.immutable.ImmutableList
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR

@Composable
internal fun RoomEditorList(
    students: ImmutableList<UiEditableStudent>,
    openDatePicker: UiOpenDatePicker?,
    onAddStudent: () -> Unit,
    onRemoveStudent: (String) -> Unit,
    onStreamNumberChange: (String, String) -> Unit,
    onCheckInDateChange: (String, String) -> Unit,
    onCheckOutDateChange: (String, String) -> Unit,
    onCheckInDateMillisChange: (String, Long) -> Unit,
    onCheckOutDateMillisChange: (String, Long) -> Unit,
    onOpenPicker: (String, UiDateField) -> Unit,
    onDismissPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = padding_size_16,
            end = padding_size_16,
            top = padding_size_16,
            bottom = padding_size_96,
        ),
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
                openField = openDatePicker
                    ?.takeIf { it.studentId == student.id }
                    ?.field,
                onStreamNumberChange = { value ->
                    onStreamNumberChange(student.id, value)
                },
                onCheckInDateMillisChange = { value ->
                    onCheckInDateMillisChange(student.id, value)
                },
                onCheckOutDateMillisChange = { value ->
                    onCheckOutDateMillisChange(student.id, value)
                },
                onOpenPicker = { field ->
                    onOpenPicker(student.id, field)
                },
                onDismissPicker = onDismissPicker,
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
                    width = size_1,
                    color = ResideTrackTheme.colors.borderDefault,
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ResideTrackTheme.colors.textPrimary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(MR.strings.add_student),
                )
                Spacer(modifier = Modifier.width(padding_size_8))
                Text(
                    text = stringResource(MR.strings.add_student),
                    style = ResideTrackTheme.typography.paragraph,
                )
            }
        }

        if (students.isEmpty()) {
            item {
                EmptyStudentsState()
            }
        }
    }
}
