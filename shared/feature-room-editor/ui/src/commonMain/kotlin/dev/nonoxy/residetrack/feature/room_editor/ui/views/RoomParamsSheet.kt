package dev.nonoxy.residetrack.feature.room_editor.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.common.button.ResideTrackButton
import dev.nonoxy.residetrack.common.ui.common.textfield.ResideTrackTextField
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_4
import dev.nonoxy.residetrack.common.ui.theme.padding_size_12
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import dev.nonoxy.residetrack.common.ui.theme.padding_size_24
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiRoomParams
import dev.nonoxy.residetrack.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomParamsSheet(
    params: UiRoomParams,
    onFloorChange: (String) -> Unit,
    onRoomNumberChange: (String) -> Unit,
    onBedsChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ResideTrackTheme.colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = padding_size_24)
                .padding(bottom = padding_size_24),
            verticalArrangement = Arrangement.spacedBy(padding_size_16),
        ) {
            Text(
                text = stringResource(MR.strings.room_editor_params_title),
                style = ResideTrackTheme.typography.head2,
                color = ResideTrackTheme.colors.textPrimary,
            )

            ResideTrackTextField(
                value = params.floorNumber,
                onValueChange = onFloorChange,
                label = stringResource(MR.strings.add_room_floor_number_label),
                placeholder = stringResource(MR.strings.add_room_floor_number_placeholder),
                keyboardType = KeyboardType.Number,
            )
            ResideTrackTextField(
                value = params.roomNumber,
                onValueChange = onRoomNumberChange,
                label = stringResource(MR.strings.add_room_room_number_label),
                placeholder = stringResource(MR.strings.add_room_room_number_placeholder),
                keyboardType = KeyboardType.Number,
                isError = params.roomNumberError,
                errorMessage = if (params.roomNumberError) {
                    stringResource(MR.strings.error_room_number_taken)
                } else {
                    null
                },
            )
            ResideTrackTextField(
                value = params.bedsCount,
                onValueChange = onBedsChange,
                label = stringResource(MR.strings.add_room_beds_count_label),
                placeholder = stringResource(MR.strings.add_room_beds_count_placeholder),
                keyboardType = KeyboardType.Number,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = padding_size_12),
                verticalArrangement = Arrangement.spacedBy(padding_size_4),
            ) {
                ResideTrackButton(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(MR.strings.room_editor_save_params),
                        style = ResideTrackTheme.typography.paragraph,
                        color = ResideTrackTheme.colors.white,
                    )
                }

                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(MR.strings.room_editor_delete_room),
                        color = ResideTrackTheme.colors.textError,
                    )
                }
            }
        }
    }
}
