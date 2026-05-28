package dev.nonoxy.feature.add_room.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.nonoxy.feature.add_room.presentation.models.UiAddRoomState
import dev.nonoxy.feature.add_room.ui.localized
import dev.nonoxy.residetrack.common.ui.common.textfield.ResideTrackTextField
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun RoomNumberSection(
    textFieldState: UiAddRoomState.TextField,
    isLoading: Boolean,
    onInputValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ResideTrackTextField(
        value = textFieldState.value,
        onValueChange = onInputValueChange,
        label = stringResource(MR.strings.add_room_room_number_label),
        placeholder = stringResource(MR.strings.add_room_room_number_placeholder),
        keyboardType = KeyboardType.Number,
        isError = textFieldState.errorKind != null,
        errorMessage = textFieldState.errorKind?.localized(),
        enabled = !isLoading,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun RoomNumberSectionPreview() {
    ResideTrackTheme {
        RoomNumberSection(
            textFieldState = UiAddRoomState.TextField(value = "301"),
            isLoading = false,
            onInputValueChange = {},
        )
    }
}
