package dev.nonoxy.core.design.common.datepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import dev.nonoxy.core.design.theme.ResideTrackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResideTrackDatePicker(
    showDatePicker: Boolean,
    onShowDatePickerStateChange: (showDatePicker: Boolean) -> Unit,
    value: String,
    placeholder: String,
    selectedDateMillis: Long?,
    onDateSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    saveButtonText: String = "Сохранить",
    cancelButtonText: String = "Отмена"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = ResideTrackTheme.colors.surface,
                shape = ResideTrackTheme.shapes.cornerRadius10
            )
            .clip(ResideTrackTheme.shapes.cornerRadius10)
            .clickable { onShowDatePickerStateChange(true) }
    ) {
        Text(
            text = value.ifBlank { placeholder },
            style = ResideTrackTheme.typography.paragraph.copy(
                color = if (value.isBlank()) {
                    ResideTrackTheme.colors.textCaption
                } else {
                    ResideTrackTheme.colors.textPrimary
                }
            )
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        val confirmEnabled by remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }

        DatePickerDialog(
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
                colors = DatePickerDefaults.colors(
                    containerColor = ResideTrackTheme.colors.surface
                )
            )
        }
    }
}
