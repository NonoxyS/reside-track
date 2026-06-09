package dev.nonoxy.residetrack.common.ui.common.datepicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_12
import dev.nonoxy.residetrack.common.ui.theme.size_24
import dev.nonoxy.residetrack.common.ui.theme.size_48
import dev.nonoxy.residetrack.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResideTrackDatePicker(
    showDatePicker: Boolean,
    onShowDatePickerStateChange: (showDatePicker: Boolean) -> Unit,
    value: String,
    placeholder: String,
    onDateSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    datePickerState: DatePickerState = rememberDatePickerState(),
    saveButtonText: String = stringResource(MR.strings.save),
    cancelButtonText: String = stringResource(MR.strings.cancel),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = size_48)
            .clip(ResideTrackTheme.shapes.cornerRadius10)
            .background(
                color = ResideTrackTheme.colors.surface,
                shape = ResideTrackTheme.shapes.cornerRadius10
            )
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = ResideTrackTheme.colors.borderDefault
                ),
                shape = ResideTrackTheme.shapes.cornerRadius10
            )
            .clickable { onShowDatePickerStateChange(true) }
            .padding(horizontal = padding_size_12),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = value.ifBlank { placeholder },
            style = ResideTrackTheme.typography.paragraph.copy(
                color = if (value.isBlank()) {
                    ResideTrackTheme.colors.textCaption
                } else {
                    ResideTrackTheme.colors.textPrimary
                }
            )
        )

        Icon(
            modifier = Modifier.size(size_24),
            imageVector = Icons.Default.DateRange,
            contentDescription = stringResource(MR.strings.select_date),
            tint = ResideTrackTheme.colors.textCaption
        )
    }

    if (showDatePicker) {
        val confirmEnabled by remember(datePickerState) {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }

        DatePickerDialog(
            colors = datePickerColors,
            onDismissRequest = { onShowDatePickerStateChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelect(millis)
                        }
                        onShowDatePickerStateChange(false)
                    },
                    enabled = confirmEnabled
                ) {
                    Text(
                        text = saveButtonText,
                        color = if (confirmEnabled) {
                            ResideTrackTheme.colors.textPrimary
                        } else {
                            ResideTrackTheme.colors.textCaption
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onShowDatePickerStateChange(false) }
                ) {
                    Text(
                        text = cancelButtonText,
                        color = ResideTrackTheme.colors.textCaption
                    )
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = datePickerColors
            )
        }
    }
}

private val datePickerColors
    @Composable get() = DatePickerDefaults.colors(
        containerColor = ResideTrackTheme.colors.surface,
        titleContentColor = ResideTrackTheme.colors.textBody,
        headlineContentColor = ResideTrackTheme.colors.textPrimary,
        weekdayContentColor = ResideTrackTheme.colors.textCaption,
        subheadContentColor = ResideTrackTheme.colors.textBody,
        navigationContentColor = ResideTrackTheme.colors.textPrimary,
        yearContentColor = ResideTrackTheme.colors.textBody,
        currentYearContentColor = ResideTrackTheme.colors.textAccent,
        selectedYearContentColor = ResideTrackTheme.colors.white,
        selectedYearContainerColor = ResideTrackTheme.colors.textAccent,
        dayContentColor = ResideTrackTheme.colors.textPrimary,
        disabledDayContentColor = ResideTrackTheme.colors.textDisable,
        selectedDayContentColor = ResideTrackTheme.colors.white,
        selectedDayContainerColor = ResideTrackTheme.colors.textAccent,
        todayContentColor = ResideTrackTheme.colors.textAccent,
        todayDateBorderColor = ResideTrackTheme.colors.textAccent,
        dividerColor = ResideTrackTheme.colors.borderDefault,
        dateTextFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ResideTrackTheme.colors.textPrimary,
            unfocusedTextColor = ResideTrackTheme.colors.textPrimary,
            cursorColor = ResideTrackTheme.colors.textAccent,
            focusedBorderColor = ResideTrackTheme.colors.borderActive,
            unfocusedBorderColor = ResideTrackTheme.colors.borderDefault,
            focusedLabelColor = ResideTrackTheme.colors.textAccent,
            unfocusedLabelColor = ResideTrackTheme.colors.textCaption,
            focusedPlaceholderColor = ResideTrackTheme.colors.textCaption,
            unfocusedPlaceholderColor = ResideTrackTheme.colors.textCaption,
            errorTextColor = ResideTrackTheme.colors.textPrimary,
            errorBorderColor = ResideTrackTheme.colors.borderError,
            errorLabelColor = ResideTrackTheme.colors.textError,
            errorCursorColor = ResideTrackTheme.colors.textError,
        ),
    )
