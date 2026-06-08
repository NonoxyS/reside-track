package dev.nonoxy.residetrack.feature.upcoming.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_32
import dev.nonoxy.residetrack.common.ui.theme.padding_size_8
import dev.nonoxy.residetrack.res.MR
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun UpcomingEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(padding_size_32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(MR.strings.upcoming_empty_title),
            style = ResideTrackTheme.typography.head4,
            color = ResideTrackTheme.colors.textCaption,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(padding_size_8))
        Text(
            text = stringResource(MR.strings.upcoming_empty_hint),
            style = ResideTrackTheme.typography.paragraph,
            color = ResideTrackTheme.colors.textCaption,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        UpcomingEmptyState(modifier = Modifier.fillMaxSize())
    }
}
