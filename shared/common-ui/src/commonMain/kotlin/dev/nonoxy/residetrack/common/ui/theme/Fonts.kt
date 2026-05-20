package dev.nonoxy.residetrack.common.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import residetrack.shared.common_ui.generated.resources.Poppins_Bold
import residetrack.shared.common_ui.generated.resources.Poppins_ExtraBold
import residetrack.shared.common_ui.generated.resources.Poppins_Medium
import residetrack.shared.common_ui.generated.resources.CommonUiRes

internal val fontPoppins: FontFamily
    @Composable get() = FontFamily(
        Font(resource = CommonUiRes.font.Poppins_Medium, weight = FontWeight.Medium),
        Font(resource = CommonUiRes.font.Poppins_Bold, weight = FontWeight.Bold),
        Font(resource = CommonUiRes.font.Poppins_ExtraBold, weight = FontWeight.ExtraBold),
    )
