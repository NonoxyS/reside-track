package dev.nonoxy.residetrack.feature.rooms.ui.views

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(MR.strings.backup_menu_export),
                    style = ResideTrackTheme.typography.paragraph,
                    color = ResideTrackTheme.colors.textPrimary,
                )
            },
            onClick = {
                onDismiss()
                onExportClick()
            },
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(MR.strings.backup_menu_import),
                    style = ResideTrackTheme.typography.paragraph,
                    color = ResideTrackTheme.colors.textPrimary,
                )
            },
            onClick = {
                onDismiss()
                onImportClick()
            },
        )
    }
}
