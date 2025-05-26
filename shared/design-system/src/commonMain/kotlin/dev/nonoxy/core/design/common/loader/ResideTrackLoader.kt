package dev.nonoxy.core.design.common.loader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_16
import dev.nonoxy.core.design.theme.size_44
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ResideTrackLoader(
    modifier: Modifier = Modifier,
    color: Color = ResideTrackTheme.colors.primary,
    size: Dp = size_44,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth
    )
}

@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        Box(modifier = Modifier.padding(padding_size_16)) {
            ResideTrackLoader()
        }
    }
}
