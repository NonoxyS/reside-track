package dev.nonoxy.core.design.common.textfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_4
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ResideTrackTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    style = ResideTrackTheme.typography.paragraph
                )
            },
            placeholder = placeholder?.let { placeholderText ->
                {
                    Text(
                        text = placeholderText,
                        style = ResideTrackTheme.typography.paragraph.copy(
                            color = ResideTrackTheme.colors.textCaption
                        )
                    )
                }
            },
            isError = isError,
            singleLine = singleLine,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = ResideTrackTheme.shapes.cornerRadius10,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ResideTrackTheme.colors.borderActive,
                unfocusedBorderColor = ResideTrackTheme.colors.borderDefault,
                errorBorderColor = ResideTrackTheme.colors.borderError,
                focusedLabelColor = ResideTrackTheme.colors.textPrimary,
                unfocusedLabelColor = ResideTrackTheme.colors.textCaption,
                errorLabelColor = ResideTrackTheme.colors.textError,
                focusedTextColor = ResideTrackTheme.colors.textPrimary,
                unfocusedTextColor = ResideTrackTheme.colors.textBody,
                errorTextColor = ResideTrackTheme.colors.textPrimary,
                focusedContainerColor = ResideTrackTheme.colors.surface,
                unfocusedContainerColor = ResideTrackTheme.colors.surface,
                disabledContainerColor = ResideTrackTheme.colors.fillInactive.copy(alpha = 0.1f)
            ),
            textStyle = ResideTrackTheme.typography.paragraph,
            modifier = Modifier.fillMaxWidth()
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = ResideTrackTheme.typography.paragraphSM.copy(
                    color = ResideTrackTheme.colors.textError
                ),
                modifier = Modifier.padding(
                    start = padding_size_4,
                    top = padding_size_4
                )
            )
        }
    }
}

@Preview
@Composable
private fun ResideTrackTextFieldPreview() {
    ResideTrackTheme {
        Column {
            ResideTrackTextField(
                value = "123",
                onValueChange = {},
                label = "Номер комнаты",
                placeholder = "Введите номер комнаты"
            )

            ResideTrackTextField(
                value = "invalid",
                onValueChange = {},
                label = "Номер этажа",
                placeholder = "Введите номер этажа",
                isError = true,
                errorMessage = "Номер этажа должен быть числом",
                modifier = Modifier.padding(top = padding_size_4)
            )
        }
    }
}
