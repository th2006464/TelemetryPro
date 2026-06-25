package com.telemetrypro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
 * @param unit optional unit string displayed smaller after value (below by default, inline when unitInline=true)
 * @param secondaryValue optional second value line rendered in the SAME style as the primary value
 * @param secondaryUnit optional unit for the secondary value (inline, same style as primary unit)
 * @param subLabel optional secondary line below value (small CodeSm style)
 * @param unitInline when true, unit is displayed on the same row to the right of value
 */
@Composable
fun ReadoutTile(
    label: String,
    value: String,
    unit: String = "",
    secondaryValue: String = "",
    secondaryUnit: String = "",
    subLabel: String = "",
    unitInline: Boolean = false,
    compact: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = PrimaryFixedDim,
    subLabelColor: androidx.compose.ui.graphics.Color? = null,
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
            val valueStyle = if (compact) TelemetryMd else if (value.length <= 6) DisplayData else TelemetryMd
            if (unitInline && unit.isNotEmpty()) {
                // Unit inline — value and unit on same row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value,
                        style = valueStyle,
                        color = valueColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = LabelCaps,
                        color = OnSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = buildString {
                        append(value)
                        if (unit.isNotEmpty()) append(" ")
                    },
                    style = valueStyle,
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
            }
            // Secondary value line — rendered in the SAME style as the primary value+unit
            if (secondaryValue.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = secondaryValue,
                        style = valueStyle,
                        color = valueColor,
                        fontWeight = FontWeight.Bold
                    )
                    if (secondaryUnit.isNotEmpty()) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = secondaryUnit,
                            style = LabelCaps,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
            if (subLabel.isNotEmpty()) {
                Text(
                    text = subLabel,
                    style = CodeSm,
                    color = subLabelColor ?: OnSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
