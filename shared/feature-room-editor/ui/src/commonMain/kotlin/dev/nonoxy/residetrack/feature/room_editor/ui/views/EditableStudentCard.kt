package dev.nonoxy.residetrack.feature.room_editor.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiDateField
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiEditableStudent
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_12
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16

@Composable
internal fun EditableStudentCard(
    student: UiEditableStudent,
    openField: UiDateField?,
    onStreamNumberChange: (String) -> Unit,
    onCheckInDateMillisChange: (Long) -> Unit,
    onCheckOutDateMillisChange: (Long) -> Unit,
    onOpenPicker: (UiDateField) -> Unit,
    onDismissPicker: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (student.isNew) {
                ResideTrackTheme.colors.fillSecondary
            } else {
                ResideTrackTheme.colors.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding_size_16),
        ) {
            StudentCardHeader(
                streamNumber = student.streamNumber,
                isNew = student.isNew,
                onRemove = onRemove,
            )

            Spacer(modifier = Modifier.height(padding_size_12))

            StudentCardContent(
                streamNumber = student.streamNumber,
                checkInDate = student.checkInDate,
                checkOutDate = student.checkOutDate,
                checkInDateMillis = student.checkInDateMillis,
                checkOutDateMillis = student.checkOutDateMillis,
                openField = openField,
                onStreamNumberChange = onStreamNumberChange,
                onCheckInDateMillisChange = onCheckInDateMillisChange,
                onCheckOutDateMillisChange = onCheckOutDateMillisChange,
                onOpenPicker = onOpenPicker,
                onDismissPicker = onDismissPicker,
            )
        }
    }
}
