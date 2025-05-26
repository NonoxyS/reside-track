package dev.nonoxy.core.design.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalResideTrackShapes = staticCompositionLocalOf<ResideTrackShapes> {
    error("CompositionLocal LocalResideTrackShapes was not provided")
}

@Immutable
data class ResideTrackShapes(
    val cornerRadius4: CornerBasedShape,
    val cornerRadius6: CornerBasedShape,
    val cornerRadius8: CornerBasedShape,
    val cornerRadius10: CornerBasedShape,
    val cornerRadius12: CornerBasedShape,
    val cornerRadius16: CornerBasedShape,
    val cornerRadius20: CornerBasedShape,
    val cornerRadius22: CornerBasedShape,
    val cornerRadius24: CornerBasedShape,
    val cornerRadius40: CornerBasedShape,
)

internal val ResideShapes = ResideTrackShapes(
    cornerRadius4 = RoundedCornerShape(corner_radius_4),
    cornerRadius6 = RoundedCornerShape(corner_radius_6),
    cornerRadius8 = RoundedCornerShape(corner_radius_8),
    cornerRadius10 = RoundedCornerShape(corner_radius_10),
    cornerRadius12 = RoundedCornerShape(corner_radius_12),
    cornerRadius16 = RoundedCornerShape(corner_radius_16),
    cornerRadius20 = RoundedCornerShape(corner_radius_20),
    cornerRadius22 = RoundedCornerShape(corner_radius_22),
    cornerRadius24 = RoundedCornerShape(corner_radius_24),
    cornerRadius40 = RoundedCornerShape(corner_radius_40),
)
