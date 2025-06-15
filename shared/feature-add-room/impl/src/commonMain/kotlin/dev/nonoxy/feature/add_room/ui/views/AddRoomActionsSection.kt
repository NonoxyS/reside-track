package dev.nonoxy.feature.add_room.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.core.design.common.button.ResideTrackButton
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_32
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.feature_add_room.impl.generated.resources.Res
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_cancel_button
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_create_button

@Composable
internal fun AddRoomActionsSection(
    isFormValid: Boolean,
    isLoading: Boolean,
    onCancelClick: () -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(padding_size_32)
    ) {
        ResideTrackButton(
            onClick = onCancelClick,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = ResideTrackTheme.colors.fillSecondary,
                disabledContainerColor = ResideTrackTheme.colors.fillInactive
            )
        ) {
            Text(
                text = stringResource(Res.string.add_room_cancel_button),
                style = ResideTrackTheme.typography.head3.copy(
                    color = ResideTrackTheme.colors.textPrimary
                )
            )
        }

        ResideTrackButton(
            onClick = onCreateClick,
            modifier = Modifier.weight(1f),
            enabled = isFormValid && !isLoading,
            loading = isLoading
        ) {
            Text(
                text = stringResource(Res.string.add_room_create_button),
                style = ResideTrackTheme.typography.head3.copy(
                    color = ResideTrackTheme.colors.textPrimary
                )
            )
        }
    }
}

@Preview
@Composable
private fun AddRoomActionsSectionPreview() {
    ResideTrackTheme {
        AddRoomActionsSection(
            isFormValid = true,
            isLoading = false,
            onCancelClick = {},
            onCreateClick = {}
        )
    }
}
