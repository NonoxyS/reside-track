package dev.nonoxy.residetrack.feature.rooms.ui.views

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.res.MR

@Composable
internal fun BackupMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = ResideTrackTheme.colors.surface,
        shape = ResideTrackTheme.shapes.cornerRadius12,
    ) {
        BackupMenuItem(
            text = stringResource(MR.strings.backup_menu_export),
            icon = MR.images.ic_backup_export,
            color = ResideTrackTheme.colors.textPrimary,
            onClick = {
                onDismiss()
                onExportClick()
            },
        )
        // Restore is destructive (replace-all), so it reads in the error colour before it's tapped.
        BackupMenuItem(
            text = stringResource(MR.strings.backup_menu_import),
            icon = MR.images.ic_backup_restore,
            color = ResideTrackTheme.colors.textError,
            onClick = {
                onDismiss()
                onImportClick()
            },
        )
    }
}

@Composable
private fun BackupMenuItem(
    text: String,
    icon: ImageResource,
    color: Color,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = ResideTrackTheme.typography.paragraph,
                color = color,
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = color,
            )
        },
        onClick = onClick,
    )
}
