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
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.feature_add_room.impl.generated.resources.Res
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_beds_count_label
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_beds_count_placeholder
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_select_beds_hint

@Composable
internal fun BedsSelectionSection(
    textFieldState: TextFieldState,
    existingBedsCounts: ImmutableList<Int>,
    showInput: Boolean,
    isLoading: Boolean,
    onInputValueChange: (String) -> Unit,
    onBedsSelect: (Int) -> Unit,
    onToggleInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(padding_size_16)
    ) {
        if (existingBedsCounts.isNotEmpty()) {
            Column {
                AnimatedVisibility(visible = !showInput) {
                    Text(
                        text = stringResource(Res.string.add_room_select_beds_hint),
                        style = ResideTrackTheme.typography.head3.copy(
                            color = ResideTrackTheme.colors.textCaption
                        )
                    )
                }
            }

            BedsSelectionChips(
                existingBedsCounts = existingBedsCounts,
                selectedBedsCount = textFieldState.value,
                showInput = showInput,
                onBedsSelect = onBedsSelect,
                onToggleInput = onToggleInput
            )
        }

        Column {
            AnimatedVisibility(visible = showInput || existingBedsCounts.isEmpty()) {
                ResideTrackTextField(
                    value = textFieldState.value,
                    onValueChange = onInputValueChange,
                    label = stringResource(Res.string.add_room_beds_count_label),
                    placeholder = stringResource(Res.string.add_room_beds_count_placeholder),
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
private fun BedsSelectionSectionPreview() {
    ResideTrackTheme {
        BedsSelectionSection(
            textFieldState = TextFieldState(value = "2"),
            existingBedsCounts = persistentListOf(1, 2, 3, 4),
            showInput = false,
            isLoading = false,
            onInputValueChange = {},
            onBedsSelect = {},
            onToggleInput = {}
        )
    }
}
