package dev.nonoxy.feature.add_room.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.nonoxy.core.design.common.textfield.ResideTrackTextField
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_16
import dev.nonoxy.feature.add_room.presentation.models.TextFieldState
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.feature_add_room.impl.generated.resources.Res
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_floor_number_label
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_floor_number_placeholder
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_select_floor_hint
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun FloorSelectionSection(
    textFieldState: TextFieldState,
    existingFloors: ImmutableList<Int>,
    showInput: Boolean,
    hasExistingRooms: Boolean,
    isLoading: Boolean,
    onInputValueChange: (String) -> Unit,
    onFloorSelect: (Int) -> Unit,
    onToggleInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(padding_size_16)
    ) {
        if (hasExistingRooms && existingFloors.isNotEmpty()) {
            Column {
                AnimatedVisibility(visible = !showInput) {
                    Text(
                        text = stringResource(Res.string.add_room_select_floor_hint),
                        style = ResideTrackTheme.typography.head3.copy(
                            color = ResideTrackTheme.colors.textCaption
                        )
                    )
                }
            }

            FloorSelectionChips(
                existingFloors = existingFloors,
                selectedFloor = textFieldState.value,
                showInput = showInput,
                onFloorSelect = onFloorSelect,
                onToggleInput = onToggleInput
            )
        }

        Column {
            AnimatedVisibility(visible = showInput || !hasExistingRooms) {
                ResideTrackTextField(
                    value = textFieldState.value,
                    onValueChange = onInputValueChange,
                    label = stringResource(Res.string.add_room_floor_number_label),
                    placeholder = stringResource(Res.string.add_room_floor_number_placeholder),
                    keyboardType = KeyboardType.Number,
                    isError = textFieldState.isError,
                    errorMessage = textFieldState.errorMessage,
                    enabled = !isLoading
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
            textFieldState = TextFieldState(value = "2"),
            existingFloors = persistentListOf(3, 4, 5, 6, 7),
            showInput = false,
            hasExistingRooms = true,
            isLoading = false,
            onInputValueChange = {},
            onFloorSelect = {},
            onToggleInput = {}
        )
    }
}
