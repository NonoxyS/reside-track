package dev.nonoxy.feature.add_room.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.nonoxy.core.design.common.textfield.ResideTrackTextField
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.feature.add_room.presentation.models.TextFieldState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.feature_add_room.impl.generated.resources.Res
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_room_number_label
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_room_number_placeholder

@Composable
internal fun RoomNumberSection(
    textFieldState: TextFieldState,
    isLoading: Boolean,
    onInputValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ResideTrackTextField(
        value = textFieldState.value,
        onValueChange = onInputValueChange,
        label = stringResource(Res.string.add_room_room_number_label),
        placeholder = stringResource(Res.string.add_room_room_number_placeholder),
        keyboardType = KeyboardType.Number,
        isError = textFieldState.isError,
        errorMessage = textFieldState.errorMessage,
        enabled = !isLoading,
        modifier = modifier
    )
}

@Preview
@Composable
private fun RoomNumberSectionPreview() {
    ResideTrackTheme {
        RoomNumberSection(
            textFieldState = TextFieldState(value = "301"),
            isLoading = false,
            onInputValueChange = {}
        )
    }
}
