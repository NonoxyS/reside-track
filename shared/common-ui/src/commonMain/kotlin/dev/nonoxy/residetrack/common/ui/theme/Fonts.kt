package dev.nonoxy.residetrack.common.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.icerock.moko.resources.compose.asFont
import dev.nonoxy.residetrack.res.MR

internal val fontPoppins: FontFamily
    @Composable get() = FontFamily(
        listOfNotNull(
            MR.fonts.poppins_medium.asFont(weight = FontWeight.Medium),
            MR.fonts.poppins_bold.asFont(weight = FontWeight.Bold),
            MR.fonts.poppins_extrabold.asFont(weight = FontWeight.ExtraBold),
        )
    )
