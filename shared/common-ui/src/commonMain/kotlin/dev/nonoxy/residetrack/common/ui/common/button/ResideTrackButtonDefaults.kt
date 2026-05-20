package dev.nonoxy.residetrack.common.ui.common.button

import androidx.compose.foundation.layout.PaddingValues
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import dev.nonoxy.residetrack.common.ui.theme.padding_size_8

object ResideTrackButtonDefaults {

    private val ButtonHorizontalPadding = padding_size_16
    private val ButtonVerticalPadding = padding_size_8

    val ContentPadding: PaddingValues =
        PaddingValues(
            horizontal = ButtonHorizontalPadding,
            vertical = ButtonVerticalPadding
        )
}
