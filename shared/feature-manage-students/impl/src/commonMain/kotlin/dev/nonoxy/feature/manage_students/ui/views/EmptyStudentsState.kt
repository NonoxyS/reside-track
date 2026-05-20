package dev.nonoxy.feature.manage_students.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_32
import dev.nonoxy.core.design.theme.padding_size_8
import org.jetbrains.compose.resources.stringResource
import residetrack.shared.feature_manage_students.impl.generated.resources.Res
import residetrack.shared.feature_manage_students.impl.generated.resources.no_students_added
import residetrack.shared.feature_manage_students.impl.generated.resources.no_students_hint

@Composable
internal fun EmptyStudentsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(padding_size_32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.no_students_added),
            style = ResideTrackTheme.typography.head4,
            color = ResideTrackTheme.colors.textCaption,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(padding_size_8))
        Text(
            text = stringResource(Res.string.no_students_hint),
            style = ResideTrackTheme.typography.paragraph,
            color = ResideTrackTheme.colors.textCaption,
            textAlign = TextAlign.Center
        )
    }
} 