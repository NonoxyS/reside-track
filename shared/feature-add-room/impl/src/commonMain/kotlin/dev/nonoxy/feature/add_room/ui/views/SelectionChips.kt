package dev.nonoxy.feature.add_room.ui.views

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_16
import dev.nonoxy.core.design.theme.padding_size_8
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.feature_add_room.impl.generated.resources.Res
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_custom_beds_button
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_hide_button
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_new_floor_button

@Composable
internal fun FloorSelectionChips(
    existingFloors: ImmutableList<Int>,
    selectedFloor: String,
    showInput: Boolean,
    onFloorSelect: (Int) -> Unit,
    onToggleInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(padding_size_8)
    ) {
        existingFloors.fastForEach { floor ->
            FilterChip(
                onClick = { onFloorSelect(floor) },
                enabled = !showInput,
                label = {
                    Text(
                        text = floor.toString(),
                        style = ResideTrackTheme.typography.paragraph
                    )
                },
                selected = selectedFloor == floor.toString(),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = ResideTrackTheme.colors.surface,
                    selectedContainerColor = ResideTrackTheme.colors.fillPrimary,
                    labelColor = ResideTrackTheme.colors.textBody,
                    selectedLabelColor = ResideTrackTheme.colors.white,
                    disabledContainerColor = ResideTrackTheme.colors.fillInactive.copy(alpha = 0.5f),
                    disabledLabelColor = ResideTrackTheme.colors.textDisable
                )
            )
        }

        FilterChip(
            onClick = onToggleInput,
            label = {
                Text(
                    text = if (showInput) {
                        stringResource(Res.string.add_room_hide_button)
                    } else {
                        stringResource(Res.string.add_room_new_floor_button)
                    },
                    style = ResideTrackTheme.typography.paragraph
                )
            },
            selected = showInput,
            leadingIcon = {
                Text(
                    text = if (showInput) "−" else "+",
                    style = ResideTrackTheme.typography.head3.copy(
                        color = if (showInput) {
                            ResideTrackTheme.colors.white
                        } else {
                            ResideTrackTheme.colors.textAccent
                        }
                    )
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = ResideTrackTheme.colors.fillSecondary,
                selectedContainerColor = ResideTrackTheme.colors.fillPrimary,
                labelColor = ResideTrackTheme.colors.textAccent,
                selectedLabelColor = ResideTrackTheme.colors.white
            )
        )
    }
}

@Composable
internal fun BedsSelectionChips(
    existingBedsCounts: ImmutableList<Int>,
    selectedBedsCount: String,
    showInput: Boolean,
    onBedsSelect: (Int) -> Unit,
    onToggleInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(padding_size_8)
    ) {
        existingBedsCounts.fastForEach { bedsCount ->
            FilterChip(
                onClick = { onBedsSelect(bedsCount) },
                enabled = !showInput,
                label = {
                    Text(
                        text = bedsCount.toString(),
                        style = ResideTrackTheme.typography.paragraph
                    )
                },
                selected = selectedBedsCount == bedsCount.toString(),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = ResideTrackTheme.colors.surface,
                    selectedContainerColor = ResideTrackTheme.colors.fillPrimary,
                    labelColor = ResideTrackTheme.colors.textBody,
                    selectedLabelColor = ResideTrackTheme.colors.white,
                    disabledContainerColor = ResideTrackTheme.colors.fillInactive.copy(alpha = 0.5f),
                    disabledLabelColor = ResideTrackTheme.colors.textDisable
                )
            )
        }

        FilterChip(
            onClick = onToggleInput,
            label = {
                Text(
                    text = if (showInput) {
                        stringResource(Res.string.add_room_hide_button)
                    } else {
                        stringResource(Res.string.add_room_custom_beds_button)
                    },
                    style = ResideTrackTheme.typography.paragraph
                )
            },
            selected = showInput,
            leadingIcon = {
                Text(
                    text = if (showInput) "−" else "+",
                    style = ResideTrackTheme.typography.head3.copy(
                        color = if (showInput) {
                            ResideTrackTheme.colors.white
                        } else {
                            ResideTrackTheme.colors.textAccent
                        }
                    )
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = ResideTrackTheme.colors.fillSecondary,
                selectedContainerColor = ResideTrackTheme.colors.fillPrimary,
                labelColor = ResideTrackTheme.colors.textAccent,
                selectedLabelColor = ResideTrackTheme.colors.white
            )
        )
    }
}

@Preview
@Composable
private fun FloorSelectionChipsPreview() {
    ResideTrackTheme {
        Column(
            modifier = Modifier.padding(padding_size_16),
            verticalArrangement = Arrangement.spacedBy(padding_size_16)
        ) {
            Text(
                text = "Обычное состояние:",
                style = ResideTrackTheme.typography.head4,
                color = ResideTrackTheme.colors.textPrimary
            )
            FloorSelectionChips(
                existingFloors = persistentListOf(3, 4, 5, 6, 7),
                selectedFloor = "5",
                showInput = false,
                onFloorSelect = {},
                onToggleInput = {}
            )

            Text(
                text = "С открытым полем ввода:",
                style = ResideTrackTheme.typography.head4,
                color = ResideTrackTheme.colors.textPrimary
            )
            FloorSelectionChips(
                existingFloors = persistentListOf(20, 21, 22, 23),
                selectedFloor = "",
                showInput = true,
                onFloorSelect = {},
                onToggleInput = {}
            )
        }
    }
}

@Preview
@Composable
private fun BedsSelectionChipsPreview() {
    ResideTrackTheme {
        Column(
            modifier = Modifier.padding(padding_size_16),
            verticalArrangement = Arrangement.spacedBy(padding_size_16)
        ) {
            Text(
                text = "Обычное состояние:",
                style = ResideTrackTheme.typography.head4
            )
            BedsSelectionChips(
                existingBedsCounts = persistentListOf(1, 2, 3, 4),
                selectedBedsCount = "2",
                showInput = false,
                onBedsSelect = {},
                onToggleInput = {}
            )

            Text(
                text = "С открытым полем ввода:",
                style = ResideTrackTheme.typography.head4
            )
            BedsSelectionChips(
                existingBedsCounts = persistentListOf(2, 4, 6, 8, 10),
                selectedBedsCount = "",
                showInput = true,
                onBedsSelect = {},
                onToggleInput = {}
            )
        }
    }
}
