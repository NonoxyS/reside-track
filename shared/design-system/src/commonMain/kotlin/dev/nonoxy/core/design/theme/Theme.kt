package dev.nonoxy.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun ResideTrackTheme(
    content: @Composable () -> Unit
) {

    CompositionLocalProvider(
        LocalResideTrackTypography provides ResideTypography,
        LocalResideTrackColors provides ResideTrackDarkColors,
        LocalResideTrackShapes provides ResideShapes
    ) {
        MaterialTheme(
            content = content
        )
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
