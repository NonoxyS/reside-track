package dev.nonoxy.feature.manage_students.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nonoxy.residetrack.common.ui.common.button.ResideTrackButton
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_12
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR

@Composable
internal fun ManageStudentsBottomButtons(
    onCloseClick: () -> Unit,
    onSaveAndClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(padding_size_16),
        horizontalArrangement = Arrangement.spacedBy(padding_size_12),
    ) {
        OutlinedButton(
            onClick = onCloseClick,
            modifier = Modifier.weight(1f),
            border = BorderStroke(
                width = 1.dp,
                color = ResideTrackTheme.colors.borderDefault,
            ),
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
