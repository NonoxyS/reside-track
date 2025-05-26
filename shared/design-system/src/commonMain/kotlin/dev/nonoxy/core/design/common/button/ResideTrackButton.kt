package dev.nonoxy.core.design.common.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import dev.nonoxy.core.design.common.loader.ResideTrackLoader
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_16
import dev.nonoxy.core.design.theme.padding_size_4
import dev.nonoxy.core.design.theme.size_20
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ResideTrackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingIcon: (@Composable () -> Unit)? = {
        ResideTrackLoader(
            color = ResideTrackTheme.colors.white,
            size = size_20,
            strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth / 2,
            modifier = Modifier.padding(end = padding_size_4)
        )
    },
    shape: Shape = ResideTrackTheme.shapes.cornerRadius10,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = ResideTrackTheme.colors.fillPrimary,
        disabledContainerColor = ResideTrackTheme.colors.fillInactive
    ),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ResideTrackButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        if (loading && loadingIcon != null) {
            loadingIcon()
        }

        content()
    }
}

@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(padding_size_16)
        ) {
            ResideTrackButton(
                onClick = {}
            ) {
                Text(
                    text = "Button",
                    style = ResideTrackTheme.typography.paragraph.copy(
                        color = ResideTrackTheme.colors.white
                    )
                )
            }

            ResideTrackButton(
                onClick = {},
                enabled = false
            ) {
                Text(
                    text = "Disabled",
                    style = ResideTrackTheme.typography.paragraph.copy(
                        color = ResideTrackTheme.colors.white
                    )
                )
            }

            ResideTrackButton(
                onClick = {},
                loading = true
            ) {
                Text(
                    text = "Loading",
                    style = ResideTrackTheme.typography.paragraph.copy(
                        color = ResideTrackTheme.colors.white
                    )
                )
            }
        }
    }
}
