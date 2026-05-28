package dev.nonoxy.feature.add_room.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.nonoxy.feature.add_room.presentation.models.UiAddRoomState
import dev.nonoxy.feature.add_room.ui.localized
import dev.nonoxy.residetrack.common.ui.common.textfield.ResideTrackTextField
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun FloorSelectionSection(
    textFieldState: UiAddRoomState.TextField,
    existingFloors: ImmutableList<Int>,
    showInput: Boolean,
    hasExistingRooms: Boolean,
    isLoading: Boolean,
    onInputValueChange: (String) -> Unit,
    onFloorSelect: (Int) -> Unit,
    onToggleInput: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(padding_size_16),
    ) {
        if (hasExistingRooms && existingFloors.isNotEmpty()) {
            Column {
                AnimatedVisibility(visible = !showInput) {
                    Text(
                        text = stringResource(MR.strings.add_room_select_floor_hint),
                        style = ResideTrackTheme.typography.head3.copy(
                            color = ResideTrackTheme.colors.textCaption,
                        ),
                    )
                }
            }

            FloorSelectionChips(
                existingFloors = existingFloors,
                selectedFloor = textFieldState.value,
                showInput = showInput,
                onFloorSelect = onFloorSelect,
                onToggleInput = onToggleInput,
            )
        }

        Column {
            AnimatedVisibility(visible = showInput || !hasExistingRooms) {
                ResideTrackTextField(
                    value = textFieldState.value,
                    onValueChange = onInputValueChange,
                    label = stringResource(MR.strings.add_room_floor_number_label),
                    placeholder = stringResource(MR.strings.add_room_floor_number_placeholder),
                    keyboardType = KeyboardType.Number,
                    isError = textFieldState.errorKind != null,
                    errorMessage = textFieldState.errorKind?.localized(),
                    enabled = !isLoading,
                )
            }
        }
    }
}

@Preview
@Composable
private fun FloorSelectionSectionPreview() {
    ResideTrackTheme {
        FloorSelectionSection(
            textFieldState = UiAddRoomState.TextField(value = "2"),
            existingFloors = persistentListOf(3, 4, 5, 6, 7),
            showInput = false,
            hasExistingRooms = true,
            isLoading = false,
            onInputValueChange = {},
            onFloorSelect = {},
            onToggleInput = {},
        )
    }
}
