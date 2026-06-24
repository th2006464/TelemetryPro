package com.telemetrypro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.R
import com.telemetrypro.app.ui.theme.CodeSm
import com.telemetrypro.app.ui.theme.LabelCaps
import com.telemetrypro.app.ui.theme.OnSurfaceVariant
import com.telemetrypro.app.ui.theme.OnSurface
import com.telemetrypro.app.ui.theme.OutlineVariant
import com.telemetrypro.app.ui.theme.SurfaceContainerLow
import com.telemetrypro.app.ui.theme.SurfaceContainerLowest
import com.telemetrypro.app.ui.theme.Secondary

@Composable
fun TopAppBar(
    fixLabel: String = stringResource(R.string.fix_status_fixed),
    isFixed: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SurfaceContainerLow)
            .border(width = 1.dp, color = OutlineVariant)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "\uD83D\uDEE7",
                style = LabelCaps,
                color = OnSurfaceVariant
            )
            Text(
                text = stringResource(R.string.app_name),
                style = LabelCaps,
                color = OnSurface,
                letterSpacing = androidx.compose.ui.unit.TextUnit(3f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusPip(status = if (isFixed) PipStatus.ACTIVE else PipStatus.WARNING)
            Text(
                text = fixLabel,
                style = LabelCaps,
                color = if (isFixed) Secondary else OnSurfaceVariant
            )
        }
    }
}
