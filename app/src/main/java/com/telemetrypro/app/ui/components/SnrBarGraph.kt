package com.telemetrypro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.R
import com.telemetrypro.app.data.SatelliteInfo
import com.telemetrypro.app.ui.theme.*

@Composable
fun SnrBarGraph(
    satellites: List<SatelliteInfo>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.snr_signal_strength), style = LabelCaps, color = OnSurfaceVariant)
            Text(stringResource(R.string.snr_dbhz), style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.6f))
        }

        Spacer(Modifier.height(12.dp))

        if (satellites.isEmpty()) {
            Text(stringResource(R.string.snr_no_data), style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.4f))
        } else {
            val maxSnr = 50f
            val sorted = satellites.sortedByDescending { it.snr }
            val displayCount = minOf(sorted.size, 16)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                for (i in 0 until displayCount) {
                    val sat = sorted[i]
                    val ratio = (sat.snr / maxSnr).coerceIn(0f, 1f)
                    val height = (ratio * 120).coerceAtLeast(4f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${sat.snr.toInt()}",
                            style = CodeSm.copy(fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)),
                            color = OnSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(2.dp))
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height.dp)
                        ) {
                            drawRoundRect(
                                color = sat.constellation.color,
                                cornerRadius = CornerRadius(2f, 2f),
                                size = Size(size.width, size.height)
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${sat.constellation.label}${sat.svid}",
                            style = CodeSm.copy(fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp)),
                            color = sat.constellation.color,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            val legendConstellations = satellites.map { it.constellation }.distinct()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                legendConstellations.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(color = c.color)
                        }
                        Spacer(Modifier.width(3.dp))
                        Text(c.label, style = CodeSm, color = c.color)
                    }
                }
            }
        }
    }
}
