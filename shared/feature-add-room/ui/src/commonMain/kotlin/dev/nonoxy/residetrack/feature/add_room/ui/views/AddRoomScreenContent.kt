package dev.nonoxy.residetrack.feature.add_room.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import dev.nonoxy.residetrack.common.ui.theme.padding_size_20
import dev.nonoxy.residetrack.common.ui.theme.padding_size_24
import dev.nonoxy.residetrack.common.utils.unsafeLazy
import dev.nonoxy.residetrack.feature.add_room.presentation.models.UiAddRoomState
import dev.nonoxy.residetrack.res.MR
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview

private val horizontalPaddingModifier by unsafeLazy {
    Modifier.padding(horizontal = padding_size_24)
}

@Composable
internal fun AddRoomScreenContent(
    state: UiAddRoomState,
    onFloorNumberInputValueChange: (String) -> Unit,
    onFloorNumberSelect: (Int) -> Unit,
    onRoomNumberInputValueChange: (String) -> Unit,
    onBedsCountInputValueChange: (String) -> Unit,
    onBedsCountSelect: (Int) -> Unit,
    onCreateRoomClick: () -> Unit,
    onCancelClick: () -> Unit,
    onToggleFloorInput: () -> Unit,
    onToggleBedsInput: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(padding_size_20),
    ) {
        Text(
            text = stringResource(MR.strings.add_room_title),
            style = ResideTrackTheme.typography.head2.copy(
                color = ResideTrackTheme.colors.textPrimary,
            ),
            modifier = horizontalPaddingModifier
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(padding_size_16),
        ) {
            FloorSelectionSection(
                textFieldState = state.floorSelection.textField,
                existingFloors = state.floorSelection.existingFloors,
                showInput = state.floorSelection.showInput,
                hasExistingRooms = state.hasExistingRooms,
                isLoading = state.isLoading,
                onInputValueChange = onFloorNumberInputValueChange,
                onFloorSelect = onFloorNumberSelect,
                onToggleInput = onToggleFloorInput,
                titleModifier = horizontalPaddingModifier
            )

            RoomNumberSection(
                textFieldState = state.roomNumber,
                isLoading = state.isLoading,
                onInputValueChange = onRoomNumberInputValueChange,
                modifier = horizontalPaddingModifier
            )

            BedsSelectionSection(
                textFieldState = state.bedsSelection.textField,
                existingBedsCounts = state.bedsSelection.existingBedsCounts,
                showInput = state.bedsSelection.showInput,
                isLoading = state.isLoading,
                onInputValueChange = onBedsCountInputValueChange,
                onBedsSelect = onBedsCountSelect,
                onToggleInput = onToggleBedsInput,
                titleModifier = horizontalPaddingModifier
            )
        }

        Spacer(modifier = Modifier.height(padding_size_16))

        AddRoomActionsSection(
            isFormValid = state.isFormValid,
            isLoading = state.isLoading,
            onCancelClick = onCancelClick,
            onCreateClick = onCreateRoomClick,
            modifier = horizontalPaddingModifier
        )
    }
}

@Preview
@Composable
private fun AddRoomContentPreview() {
    ResideTrackTheme {
        AddRoomScreenContent(
            state = UiAddRoomState(
                floorSelection = UiAddRoomState.FloorSelection(
                    textField = UiAddRoomState.TextField(value = "3"),
                    existingFloors = persistentListOf(1, 2, 3, 4, 5),
                ),
                roomNumber = UiAddRoomState.TextField(value = "301"),
                bedsSelection = UiAddRoomState.BedsSelection(
                    textField = UiAddRoomState.TextField(value = "2"),
                    existingBedsCounts = persistentListOf(1, 2, 3, 4),
                ),
                isFormValid = true,
                hasExistingRooms = true,
            ),
            onFloorNumberInputValueChange = {},
            onFloorNumberSelect = {},
            onRoomNumberInputValueChange = {},
            onBedsCountInputValueChange = {},
            onBedsCountSelect = {},
            onCreateRoomClick = {},
            onCancelClick = {},
            onToggleFloorInput = {},
            onToggleBedsInput = {},
        )
    }
}
