package dev.nonoxy.feature.manage_students.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_12
import dev.nonoxy.core.design.theme.padding_size_16
import dev.nonoxy.feature.manage_students.presentation.models.EditableStudent

@Composable
internal fun EditableStudentCard(
    student: EditableStudent,
    onStreamNumberChange: (String) -> Unit,
    onCheckInDateMillisChange: (Long) -> Unit,
    onCheckOutDateMillisChange: (Long) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (student.isNew) {
                ResideTrackTheme.colors.fillSecondary
            } else {
                ResideTrackTheme.colors.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding_size_16)
        ) {
            StudentCardHeader(
                streamNumber = student.streamNumber,
                isNew = student.isNew,
                onRemove = onRemove
            )

            Spacer(modifier = Modifier.height(padding_size_12))

            StudentCardContent(
                streamNumber = student.streamNumber,
                checkInDate = student.checkInDate,
                checkOutDate = student.checkOutDate,
                checkInDateMillis = student.checkInDateMillis,
                checkOutDateMillis = student.checkOutDateMillis,
                onStreamNumberChange = onStreamNumberChange,
                onCheckInDateMillisChange = onCheckInDateMillisChange,
                onCheckOutDateMillisChange = onCheckOutDateMillisChange
            )
        }
    }
}
