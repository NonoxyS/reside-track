package dev.nonoxy.core.design.common.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.nonoxy.core.design.common.loader.ResideTrackLoader
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_12
import dev.nonoxy.core.design.theme.text_size_20
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.design_system.generated.resources.Res
import residetrack.shared.design_system.generated.resources.loading_data

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    loadingMessage: String? = stringResource(Res.string.loading_data)
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ResideTrackLoader()

        loadingMessage?.let {
            Text(
                modifier = Modifier.padding(
                    top = padding_size_12
                ),
                text = loadingMessage,
                style = ResideTrackTheme.typography.paragraph.copy(
                    lineHeight = text_size_20,
                    color = ResideTrackTheme.colors.textBody
                )
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        LoadingState()
    }
}
