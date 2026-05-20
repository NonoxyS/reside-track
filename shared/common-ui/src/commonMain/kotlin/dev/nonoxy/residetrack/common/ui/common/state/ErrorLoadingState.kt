package dev.nonoxy.residetrack.common.ui.common.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.common.ui.common.button.ResideTrackButton
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.common_ui.generated.resources.CommonUiRes
import residetrack.shared.common_ui.generated.resources.button_update
import residetrack.shared.common_ui.generated.resources.error_something_went_wrong

@Composable
fun ErrorLoadingState(
    modifier: Modifier = Modifier,
    errorMessage: String = stringResource(CommonUiRes.string.error_something_went_wrong),
    buttonText: String = stringResource(CommonUiRes.string.button_update),
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
