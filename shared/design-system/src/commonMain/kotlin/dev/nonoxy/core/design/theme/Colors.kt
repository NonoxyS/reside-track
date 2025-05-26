package dev.nonoxy.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LocalResideTrackColors = staticCompositionLocalOf<ResideTrackColors> {
    error("CompositionLocal LocalResideTrackColors was not provided")
}

@Immutable
data class ResideTrackColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,

    val white: Color,

    val textPrimary: Color,
    val textBody: Color,
    val textCaption: Color,
    val textDisable: Color,
    val textAccent: Color,
    val textError: Color,
    val textWarning: Color,
    val textSuccess: Color,

    val fillPrimary: Color,
    val fillPrimaryHover: Color,
    val fillPrimaryPressed: Color,
    val fillSecondary: Color,
    val fillSecondaryHover: Color,
    val fillSecondaryPressed: Color,
    val fillInactive: Color,
    val fillError: Color,
    val fillErrorBGSecondary: Color,
    val fillSuccess: Color,
    val fillSuccessBGSecondary: Color,
    val fillWarning: Color,
    val fillWarningBGSecondary: Color,

    val borderDefault: Color,
    val borderActive: Color,
    val borderError: Color,

    val shadow: Color,
    val topBarShadow: Color,
)

internal val ResideTrackDarkColors = ResideTrackColors(
    primary = Color(0xFF3A3D4A),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF5A5D6A),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF111827),
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF1F2937),
    onSurface = Color(0xFFF9FAFB),

    white = Color(0xFFFFFFFF),

    textPrimary = Color(0xFFF9FAFB),
    textBody = Color(0xFFF9FAFB),
    textCaption = Color(0xFF9CA3AF),
    textDisable = Color(0xFF6B7280),
    textAccent = Color(0xFF60A5FA),
    textError = Color(0xFFF87171),
    textWarning = Color(0xFFFBBF24),
    textSuccess = Color(0xFF34D399),

    fillPrimary = Color(0xFF3A3D4A),
    fillPrimaryHover = Color(0xFF4B5563),
    fillPrimaryPressed = Color(0xFF374151),
    fillSecondary = Color(0xFF374151),
    fillSecondaryHover = Color(0xFF4B5563),
    fillSecondaryPressed = Color(0xFF6B7280),
    fillInactive = Color(0xFF6B7280),
    fillError = Color(0xFFF87171),
    fillErrorBGSecondary = Color(0xFF7F1D1D),
    fillSuccess = Color(0xFF34D399),
    fillSuccessBGSecondary = Color(0xFF064E3B),
    fillWarning = Color(0xFFFBBF24),
    fillWarningBGSecondary = Color(0xFF78350F),

    borderDefault = Color(0xFF374151),
    borderActive = Color(0xFF60A5FA),
    borderError = Color(0xFFF87171),

    shadow = Color(0x33000000),
    topBarShadow = Color(0x1A000000),
)
