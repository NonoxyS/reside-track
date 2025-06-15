package dev.nonoxy.feature.add_room.ui.views

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
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_16
import dev.nonoxy.core.design.theme.padding_size_20
import dev.nonoxy.core.design.theme.padding_size_24
import dev.nonoxy.feature.add_room.presentation.models.AddRoomEvent
import dev.nonoxy.feature.add_room.presentation.models.AddRoomViewState
import dev.nonoxy.feature.add_room.presentation.models.TextFieldState
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.feature_add_room.impl.generated.resources.Res
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_title

@Composable
internal fun AddRoomScreenContent(
    state: AddRoomViewState,
    onObtainEvent: (AddRoomEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = padding_size_24),
        verticalArrangement = Arrangement.spacedBy(padding_size_20)
    ) {
        Text(
            text = stringResource(Res.string.add_room_title),
            style = ResideTrackTheme.typography.head2.copy(
                color = ResideTrackTheme.colors.textPrimary
            )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(padding_size_16)
        ) {
            FloorSelectionSection(
                textFieldState = state.floorSelection.textField,
                existingFloors = state.floorSelection.existingFloors,
                showInput = state.floorSelection.showInput,
                hasExistingRooms = state.hasExistingRooms,
                isLoading = state.isLoading,
                onInputValueChange = { onObtainEvent(AddRoomEvent.OnFloorNumberInputValueChange(it)) },
                onFloorSelect = { onObtainEvent(AddRoomEvent.OnFloorNumberSelect(it)) },
                onToggleInput = { onObtainEvent(AddRoomEvent.OnToggleFloorInput) }
            )

            RoomNumberSection(
                textFieldState = state.roomNumber,
                isLoading = state.isLoading,
                onInputValueChange = { onObtainEvent(AddRoomEvent.OnRoomNumberInputValueChange(it)) }
            )

            BedsSelectionSection(
                textFieldState = state.bedsSelection.textField,
                existingBedsCounts = state.bedsSelection.existingBedsCounts,
                showInput = state.bedsSelection.showInput,
                isLoading = state.isLoading,
                onInputValueChange = { onObtainEvent(AddRoomEvent.OnBedsCountInputValueChange(it)) },
                onBedsSelect = { onObtainEvent(AddRoomEvent.OnBedsCountSelect(it)) },
                onToggleInput = { onObtainEvent(AddRoomEvent.OnToggleBedsInput) }
            )
        }

        Spacer(modifier = Modifier.height(padding_size_16))

        AddRoomActionsSection(
            isFormValid = state.isFormValid,
            isLoading = state.isLoading,
            onCancelClick = { onObtainEvent(AddRoomEvent.OnCancelClick) },
            onCreateClick = { onObtainEvent(AddRoomEvent.OnCreateRoomClick) }
        )
    }
}

@Preview
@Composable
private fun AddRoomContentPreview() {
    ResideTrackTheme {
        AddRoomScreenContent(
            state = AddRoomViewState.Initial.copy(
                floorSelection = AddRoomViewState.Initial.floorSelection.copy(
                    textField = TextFieldState(value = "3"),
                    existingFloors = persistentListOf(1, 2, 3, 4, 5)
                ),
                roomNumber = TextFieldState(value = "301"),
                bedsSelection = AddRoomViewState.Initial.bedsSelection.copy(
                    textField = TextFieldState(value = "2"),
                    existingBedsCounts = persistentListOf(1, 2, 3, 4)
                ),
                isFormValid = true,
                hasExistingRooms = true
            ),
            onObtainEvent = {}
        )
    }
}
