package dev.nonoxy.core.design.common.button

import androidx.compose.foundation.layout.PaddingValues
import dev.nonoxy.core.design.theme.padding_size_16
import dev.nonoxy.core.design.theme.padding_size_8

object ResideTrackButtonDefaults {

    private val ButtonHorizontalPadding = padding_size_16
    private val ButtonVerticalPadding = padding_size_8

    val ContentPadding: PaddingValues =
        PaddingValues(
            horizontal = ButtonHorizontalPadding,
            vertical = ButtonVerticalPadding
        )
}
