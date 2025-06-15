package dev.nonoxy.core.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import residetrack.shared.design_system.generated.resources.Poppins_Bold
import residetrack.shared.design_system.generated.resources.Poppins_ExtraBold
import residetrack.shared.design_system.generated.resources.Poppins_Medium
import residetrack.shared.design_system.generated.resources.Res

internal val fontPoppins: FontFamily
    @Composable get() = FontFamily(
        Font(resource = Res.font.Poppins_Medium, weight = FontWeight.Medium),
        Font(resource = Res.font.Poppins_Bold, weight = FontWeight.Bold),
        Font(resource = Res.font.Poppins_ExtraBold, weight = FontWeight.ExtraBold),
    )
