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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.ui.theme.CodeSm
import com.telemetrypro.app.ui.theme.LabelCaps
import com.telemetrypro.app.ui.theme.OnSurfaceVariant
import com.telemetrypro.app.ui.theme.OnSurface
import com.telemetrypro.app.ui.theme.OutlineVariant
import com.telemetrypro.app.ui.theme.SurfaceContainerLow
import com.telemetrypro.app.ui.theme.SurfaceContainerLowest
import com.telemetrypro.app.ui.theme.Secondary

/**
 * TopAppBar component — "TELEMETRY PRO" branding + GPS fix status.
 * Replicates the HTML <header> from all original pages.
 *
 * @param fixLabel GPS status text (e.g. "3D FIX", "ACQUIRING", "NO SIGNAL")
 * @param isFixed whether a 3D fix is currently active (controls pip color)
 */
@Composable
fun TopAppBar(
    fixLabel: String = "3D FIX",
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
        // Left: Satellite icon + branding
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "\uD83D\uDEE7", // 🛰 satellite emoji fallback
                style = LabelCaps,
                color = OnSurfaceVariant
            )
            Text(
                text = "TELEMETRY PRO",
                style = LabelCaps,
                color = OnSurface,
                letterSpacing = androidx.compose.ui.unit.TextUnit(3f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
        }

        // Right: Fix status pip + label
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
