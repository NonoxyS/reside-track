package dev.nonoxy.core.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType

internal val LocalResideTrackTypography = staticCompositionLocalOf<ResideTrackTypography> {
    error("CompositionLocal LocalResideTrackTypography was not provided")
}

@Immutable
data class ResideTrackTypography(
    val caption: TextStyle,
    val paragraphSM: TextStyle,
    val paragraph: TextStyle,
    val lead: TextStyle,
    val head4: TextStyle,
    val head3: TextStyle,
    val head2: TextStyle,
    val head1: TextStyle,
)

internal val ResideTypography: ResideTrackTypography
    @Composable get() = ResideTrackTypography(
        caption = TextStyle(
            fontFamily = fontPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = text_size_10,
            lineHeight = text_size_15,
            letterSpacing = TextUnit(0.05F, TextUnitType.Sp),
        ),
        paragraphSM = TextStyle(
            fontFamily = fontPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = text_size_11,
            lineHeight = text_size_13,
        ),
        paragraph = TextStyle(
            fontFamily = fontPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = text_size_14,
            lineHeight = text_size_20,
            letterSpacing = TextUnit(0.01F, TextUnitType.Sp)
        ),
        lead = TextStyle(
            fontFamily = fontPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = text_size_16,
            lineHeight = text_size_24,
            letterSpacing = TextUnit(0.01F, TextUnitType.Sp)
        ),
        head4 = TextStyle(
            fontFamily = fontPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = text_size_14,
            lineHeight = text_size_20
        ),
        head3 = TextStyle(
            fontFamily = fontPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = text_size_16,
            lineHeight = text_size_24
        ),
        head2 = TextStyle(
            fontFamily = fontPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = text_size_24,
            lineHeight = text_size_36
        ),
        head1 = TextStyle(
            fontFamily = fontPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = text_size_42,
            lineHeight = text_size_44
        ),
    )
