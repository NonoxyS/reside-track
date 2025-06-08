package dev.nonoxy.core.design.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier

@Composable
fun ResideTrackTheme(
    content: @Composable () -> Unit
) {

    CompositionLocalProvider(
        LocalResideTrackTypography provides ResideTypography,
        LocalResideTrackColors provides ResideTrackDarkColors,
        LocalResideTrackShapes provides ResideShapes
    ) {
        MaterialTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = ResideTrackTheme.colors.background)
            ) {
                content()
            }
        }
    }
}

object ResideTrackTheme {

    val colors: ResideTrackColors
        @Composable
        @ReadOnlyComposable
        get() = LocalResideTrackColors.current

    val typography: ResideTrackTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalResideTrackTypography.current

    val shapes: ResideTrackShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalResideTrackShapes.current
}
