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
import dev.nonoxy.core.design.common.button.ResideTrackButton
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_12
import dev.nonoxy.core.design.theme.padding_size_16
import dev.nonoxy.feature.manage_students.presentation.models.ManageStudentsEvent
import org.jetbrains.compose.resources.stringResource
import residetrack.shared.feature_manage_students.impl.generated.resources.Res
import residetrack.shared.feature_manage_students.impl.generated.resources.cancel
import residetrack.shared.feature_manage_students.impl.generated.resources.save

@Composable
internal fun ManageStudentsBottomButtons(
    onObtainEvent: (ManageStudentsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(padding_size_16),
        horizontalArrangement = Arrangement.spacedBy(padding_size_12)
    ) {
        OutlinedButton(
            onClick = { onObtainEvent(ManageStudentsEvent.OnClose) },
            modifier = Modifier.weight(1f),
            border = BorderStroke(
                width = 1.dp,
                color = ResideTrackTheme.colors.borderDefault
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ResideTrackTheme.colors.textCaption
            )
        ) {
            Text(
                text = stringResource(Res.string.cancel),
                style = ResideTrackTheme.typography.paragraph
            )
        }

        ResideTrackButton(
            onClick = { onObtainEvent(ManageStudentsEvent.OnSaveAndClose) },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(Res.string.save),
                style = ResideTrackTheme.typography.paragraph,
                color = ResideTrackTheme.colors.white
            )
        }
    }
} 