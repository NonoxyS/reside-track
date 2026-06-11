package dev.nonoxy.residetrack.feature.room_editor.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.common.button.ResideTrackButton
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_12
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import dev.nonoxy.residetrack.common.ui.theme.padding_size_24
import dev.nonoxy.residetrack.common.ui.theme.size_1
import dev.nonoxy.residetrack.res.MR

/** Floating Cancel/Save row. Sits over the scrolling list; a top gradient scrim keeps the
 *  buttons legible as content scrolls under them. */
@Composable
internal fun EditorActionButtons(
    onCloseClick: () -> Unit,
    onSaveAndClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ResideTrackTheme.colors.background.copy(alpha = 0f),
                        ResideTrackTheme.colors.background,
                    ),
                ),
            )
            .navigationBarsPadding()
            .padding(horizontal = padding_size_16)
            .padding(top = padding_size_24, bottom = padding_size_16),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(padding_size_12)) {
            OutlinedButton(
                onClick = onCloseClick,
                modifier = Modifier.weight(1f),
                border = BorderStroke(width = size_1, color = ResideTrackTheme.colors.borderDefault),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ResideTrackTheme.colors.textCaption,
                ),
            ) {
                Text(
                    text = stringResource(MR.strings.cancel),
                    style = ResideTrackTheme.typography.paragraph,
                )
            }

            ResideTrackButton(
                onClick = onSaveAndClose,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(MR.strings.save),
                    style = ResideTrackTheme.typography.paragraph,
                    color = ResideTrackTheme.colors.white,
                )
            }
        }
    }
}
