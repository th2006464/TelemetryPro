package com.telemetrypro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.ui.theme.*

/**
 * Standard dark instrument-tile card for data readouts.
 * Replicates the HTML .instrument-tile class.
 *
 * @param label uppercase label above the value (LabelCaps style)
 * @param value large data display value
 * @param unit optional unit string displayed smaller after value
 * @param subLabel optional secondary line below value
 */
@Composable
fun ReadoutTile(
    label: String,
    value: String,
    unit: String = "",
    subLabel: String = "",
    valueColor: androidx.compose.ui.graphics.Color = PrimaryFixedDim,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = label.uppercase(),
                style = LabelCaps,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = buildString {
                    append(value)
                    if (unit.isNotEmpty()) append(" ")
                },
                style = if (value.length <= 6) DisplayData else TelemetryMd,
                color = valueColor,
                fontWeight = FontWeight.Bold
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = LabelCaps,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (subLabel.isNotEmpty()) {
                Text(
                    text = subLabel,
                    style = CodeSm,
                    color = OnSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
