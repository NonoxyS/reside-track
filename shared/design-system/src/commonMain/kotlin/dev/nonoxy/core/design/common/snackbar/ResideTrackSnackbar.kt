package dev.nonoxy.core.design.common.snackbar

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import dev.nonoxy.core.design.theme.ResideTrackTheme

enum class SnackbarType {
    SUCCESS, ERROR, INFO
}

@Composable
fun ResideTrackSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = SnackbarDefaults.shape,
    containerColor: Color = ResideTrackTheme.colors.fillSecondary,
    contentColor: Color = ResideTrackTheme.colors.white,
    actionColor: Color = ResideTrackTheme.colors.textAccent,
    actionContentColor: Color = ResideTrackTheme.colors.textAccent,
    dismissActionContentColor: Color = ResideTrackTheme.colors.textCaption
) {
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionColor = actionColor,
        actionContentColor = actionContentColor,
        dismissActionContentColor = dismissActionContentColor
    )
}

@Composable
fun ResideTrackSuccessSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = SnackbarDefaults.shape
) {
    ResideTrackSnackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = ResideTrackTheme.colors.fillSuccess,
        contentColor = ResideTrackTheme.colors.white,
        actionColor = ResideTrackTheme.colors.white,
        actionContentColor = ResideTrackTheme.colors.white,
        dismissActionContentColor = ResideTrackTheme.colors.white
    )
}

@Composable
fun ResideTrackErrorSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = SnackbarDefaults.shape
) {
    ResideTrackSnackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = ResideTrackTheme.colors.fillError,
        contentColor = ResideTrackTheme.colors.white,
        actionColor = ResideTrackTheme.colors.white,
        actionContentColor = ResideTrackTheme.colors.white,
        dismissActionContentColor = ResideTrackTheme.colors.white
    )
}
