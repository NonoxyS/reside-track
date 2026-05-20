package dev.nonoxy.feature.manage_students.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.nonoxy.residetrack.common.ui.common.datepicker.ResideTrackDatePicker
import dev.nonoxy.residetrack.common.ui.common.textfield.ResideTrackTextField
import dev.nonoxy.residetrack.common.ui.theme.padding_size_8
import org.jetbrains.compose.resources.stringResource
import residetrack.shared.feature_manage_students.impl.generated.resources.Res
import residetrack.shared.feature_manage_students.impl.generated.resources.cancel
import residetrack.shared.feature_manage_students.impl.generated.resources.check_in_date_placeholder
import residetrack.shared.feature_manage_students.impl.generated.resources.check_out_date_placeholder
import residetrack.shared.feature_manage_students.impl.generated.resources.save
import residetrack.shared.feature_manage_students.impl.generated.resources.stream_number
import residetrack.shared.feature_manage_students.impl.generated.resources.stream_number_placeholder

@Composable
internal fun StudentCardContent(
    streamNumber: String,
    checkInDate: String,
    checkOutDate: String,
    checkInDateMillis: Long?,
    checkOutDateMillis: Long?,
    onStreamNumberChange: (String) -> Unit,
    onCheckInDateMillisChange: (Long) -> Unit,
    onCheckOutDateMillisChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCheckInDatePicker by remember { mutableStateOf(false) }
    var showCheckOutDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        ResideTrackTextField(
            value = streamNumber,
            onValueChange = onStreamNumberChange,
            label = stringResource(Res.string.stream_number),
            placeholder = stringResource(Res.string.stream_number_placeholder),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(padding_size_8))

        ResideTrackDatePicker(
            showDatePicker = showCheckInDatePicker,
            onShowDatePickerStateChange = { showCheckInDatePicker = it },
            value = checkInDate,
            placeholder = stringResource(Res.string.check_in_date_placeholder),
            selectedDateMillis = checkInDateMillis,
            onDateSelect = onCheckInDateMillisChange,
            saveButtonText = stringResource(Res.string.save),
            cancelButtonText = stringResource(Res.string.cancel),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(padding_size_8))

        ResideTrackDatePicker(
            showDatePicker = showCheckOutDatePicker,
            onShowDatePickerStateChange = { showCheckOutDatePicker = it },
            value = checkOutDate,
            placeholder = stringResource(Res.string.check_out_date_placeholder),
            selectedDateMillis = checkOutDateMillis,
            onDateSelect = onCheckOutDateMillisChange,
            saveButtonText = stringResource(Res.string.save),
            cancelButtonText = stringResource(Res.string.cancel),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
