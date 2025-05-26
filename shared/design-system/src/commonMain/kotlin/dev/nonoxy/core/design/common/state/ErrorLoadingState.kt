package dev.nonoxy.core.design.common.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.nonoxy.core.design.common.button.ResideTrackButton
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_16
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.design_system.generated.resources.Res
import residetrack.shared.design_system.generated.resources.button_update
import residetrack.shared.design_system.generated.resources.error_something_went_wrong

@Composable
fun ErrorLoadingState(
    modifier: Modifier = Modifier,
    errorMessage: String = stringResource(Res.string.error_something_went_wrong),
    buttonText: String = stringResource(Res.string.button_update),
    showRetryButton: Boolean = true,
    onRetryClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = errorMessage,
            style = ResideTrackTheme.typography.paragraph.copy(
                color = ResideTrackTheme.colors.textBody
            )
        )

        if (showRetryButton) {
            ResideTrackButton(
                modifier = Modifier
                    .padding(top = padding_size_16),
                onClick = onRetryClick,
                shape = ResideTrackTheme.shapes.cornerRadius24
            ) {
                Text(
                    text = buttonText,
                    style = ResideTrackTheme.typography.lead
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        ErrorLoadingState()
    }
}
