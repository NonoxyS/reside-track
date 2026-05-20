package dev.nonoxy.feature.manage_students.ui.views

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
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_12
import dev.nonoxy.core.design.theme.padding_size_16
import dev.nonoxy.core.design.theme.padding_size_8
import dev.nonoxy.feature.manage_students.presentation.models.EditableStudent
import dev.nonoxy.feature.manage_students.presentation.models.ManageStudentsEvent
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import residetrack.shared.feature_manage_students.impl.generated.resources.Res
import residetrack.shared.feature_manage_students.impl.generated.resources.add_student
import residetrack.shared.feature_manage_students.impl.generated.resources.students_total_count

@Composable
internal fun ManageStudentsList(
    students: ImmutableList<EditableStudent>,
    onObtainEvent: (ManageStudentsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(padding_size_16),
        verticalArrangement = Arrangement.spacedBy(padding_size_12)
    ) {
        if (students.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(
                        Res.string.students_total_count,
                        students.size
                    ),
                    style = ResideTrackTheme.typography.paragraph,
                    color = ResideTrackTheme.colors.textCaption
                )
                Spacer(modifier = Modifier.height(padding_size_8))
            }
        }

        items(
            items = students,
            key = { student -> student.id }
        ) { student ->
            EditableStudentCard(
                modifier = Modifier.fillMaxWidth().animateItem(),
                student = student,
                onStreamNumberChange = { value ->
                    onObtainEvent(
                        ManageStudentsEvent.OnStreamNumberChange(
                            studentId = student.id,
                            value = value
                        )
                    )
                },
                onCheckInDateMillisChange = { value ->
                    onObtainEvent(
                        ManageStudentsEvent.OnCheckInDateMillisChange(
                            studentId = student.id,
                            millis = value
                        )
                    )
                },
                onCheckOutDateMillisChange = { value ->
                    onObtainEvent(
                        ManageStudentsEvent.OnCheckOutDateMillisChange(
                            studentId = student.id,
                            millis = value
                        )
                    )
                },
                onRemove = {
                    onObtainEvent(ManageStudentsEvent.OnRemoveStudent(student.id))
                }
            )
        }

        item {
            OutlinedButton(
                onClick = { onObtainEvent(ManageStudentsEvent.OnAddStudent) },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(
                    width = 1.dp,
                    color = ResideTrackTheme.colors.borderDefault
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ResideTrackTheme.colors.textPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(padding_size_8))
                Text(stringResource(Res.string.add_student))
            }
        }

        if (students.isEmpty()) {
            item {
                EmptyStudentsState()
            }
        }
    }
}
