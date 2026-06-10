package dev.nonoxy.residetrack.feature.room_editor.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.nonoxy.residetrack.common.ui.common.datepicker.ResideTrackDatePicker
import dev.nonoxy.residetrack.common.ui.common.textfield.ResideTrackTextField
import dev.nonoxy.residetrack.common.ui.theme.padding_size_8
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiDateField
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StudentCardContent(
    streamNumber: String,
    checkInDate: String,
    checkOutDate: String,
    checkInDateMillis: Long?,
    checkOutDateMillis: Long?,
    openField: UiDateField?,
    onStreamNumberChange: (String) -> Unit,
    onCheckInDateMillisChange: (Long) -> Unit,
    onCheckOutDateMillisChange: (Long) -> Unit,
    onOpenPicker: (UiDateField) -> Unit,
    onDismissPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Check-in must stay strictly before check-out and vice versa. Bounding each
    // picker's selectableDates makes an invalid range impossible to pick in either
    // calendar or manual-input mode. key() rebuilds the state when the opposite
    // date changes so the bound stays current.
    val checkInPickerState = key(checkOutDateMillis) {
        rememberDatePickerState(
            initialSelectedDateMillis = checkInDateMillis,
            selectableDates = datesBefore(checkOutDateMillis),
        )
    }
    val checkOutPickerState = key(checkInDateMillis) {
        rememberDatePickerState(
            initialSelectedDateMillis = checkOutDateMillis,
            selectableDates = datesAfter(checkInDateMillis),
        )
    }

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
            showDatePicker = openField == UiDateField.CHECK_IN,
            onShowDatePickerStateChange = { show ->
                if (show) onOpenPicker(UiDateField.CHECK_IN) else onDismissPicker()
            },
            value = checkInDate,
            placeholder = stringResource(MR.strings.check_in_date_placeholder),
            datePickerState = checkInPickerState,
            onDateSelect = onCheckInDateMillisChange,
            saveButtonText = stringResource(MR.strings.save),
            cancelButtonText = stringResource(MR.strings.cancel),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(padding_size_8))

        ResideTrackDatePicker(
            showDatePicker = openField == UiDateField.CHECK_OUT,
            onShowDatePickerStateChange = { show ->
                if (show) onOpenPicker(UiDateField.CHECK_OUT) else onDismissPicker()
            },
            value = checkOutDate,
            placeholder = stringResource(MR.strings.check_out_date_placeholder),
            datePickerState = checkOutPickerState,
            onDateSelect = onCheckOutDateMillisChange,
            saveButtonText = stringResource(MR.strings.save),
            cancelButtonText = stringResource(MR.strings.cancel),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Allows only days strictly before [upperExclusiveMillis] (UTC-midnight). */
@OptIn(ExperimentalMaterial3Api::class)
private fun datesBefore(upperExclusiveMillis: Long?): SelectableDates =
    object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
            upperExclusiveMillis == null || utcTimeMillis < upperExclusiveMillis
    }

/** Allows only days strictly after [lowerExclusiveMillis] (UTC-midnight). */
@OptIn(ExperimentalMaterial3Api::class)
private fun datesAfter(lowerExclusiveMillis: Long?): SelectableDates =
    object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
            lowerExclusiveMillis == null || utcTimeMillis > lowerExclusiveMillis
    }
