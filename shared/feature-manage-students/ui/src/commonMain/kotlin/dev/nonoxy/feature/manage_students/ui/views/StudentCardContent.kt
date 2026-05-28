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
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR

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
            label = stringResource(MR.strings.stream_number),
            placeholder = stringResource(MR.strings.stream_number_placeholder),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(padding_size_8))

        ResideTrackDatePicker(
            showDatePicker = showCheckInDatePicker,
            onShowDatePickerStateChange = { showCheckInDatePicker = it },
            value = checkInDate,
            placeholder = stringResource(MR.strings.check_in_date_placeholder),
            selectedDateMillis = checkInDateMillis,
            onDateSelect = onCheckInDateMillisChange,
            saveButtonText = stringResource(MR.strings.save),
            cancelButtonText = stringResource(MR.strings.cancel),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(padding_size_8))

        ResideTrackDatePicker(
            showDatePicker = showCheckOutDatePicker,
            onShowDatePickerStateChange = { showCheckOutDatePicker = it },
            value = checkOutDate,
            placeholder = stringResource(MR.strings.check_out_date_placeholder),
            selectedDateMillis = checkOutDateMillis,
            onDateSelect = onCheckOutDateMillisChange,
            saveButtonText = stringResource(MR.strings.save),
            cancelButtonText = stringResource(MR.strings.cancel),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
