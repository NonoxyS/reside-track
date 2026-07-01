package dev.nonoxy.residetrack.feature.add_room.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.common.textfield.ResideTrackTextField
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import dev.nonoxy.residetrack.feature.add_room.presentation.models.UiAddRoomState
import dev.nonoxy.residetrack.res.MR
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun BedsSelectionSection(
    textFieldState: UiAddRoomState.TextField,
    existingBedsCounts: ImmutableList<Int>,
    showInput: Boolean,
    isLoading: Boolean,
    onInputValueChange: (String) -> Unit,
    onBedsSelect: (Int) -> Unit,
    onToggleInput: () -> Unit,
    titleModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(padding_size_16),
    ) {
        if (existingBedsCounts.isNotEmpty()) {
            Column {
                AnimatedVisibility(visible = !showInput) {
                    Text(
                        text = stringResource(MR.strings.add_room_select_beds_hint),
                        style = ResideTrackTheme.typography.head3.copy(
                            color = ResideTrackTheme.colors.textCaption,
                        ),
                        modifier = titleModifier,
                    )
                }
            }

            BedsSelectionChips(
                existingBedsCounts = existingBedsCounts,
                selectedBedsCount = textFieldState.value,
                showInput = showInput,
                onBedsSelect = onBedsSelect,
                onToggleInput = onToggleInput,
            )
        }

        Column {
            AnimatedVisibility(visible = showInput || existingBedsCounts.isEmpty()) {
                ResideTrackTextField(
                    value = textFieldState.value,
                    onValueChange = onInputValueChange,
                    label = stringResource(MR.strings.add_room_beds_count_label),
                    placeholder = stringResource(MR.strings.add_room_beds_count_placeholder),
                    keyboardType = KeyboardType.Number,
                    isError = textFieldState.error != null,
                    errorMessage = textFieldState.error?.let { stringResource(it.message) },
                    enabled = !isLoading,
                )
            }
        }
    }
}

@Preview
@Composable
private fun BedsSelectionSectionPreview() {
    ResideTrackTheme {
        BedsSelectionSection(
            textFieldState = UiAddRoomState.TextField(value = "2"),
            existingBedsCounts = persistentListOf(1, 2, 3, 4),
            showInput = false,
            isLoading = false,
            onInputValueChange = {},
            onBedsSelect = {},
            onToggleInput = {},
        )
    }
}
