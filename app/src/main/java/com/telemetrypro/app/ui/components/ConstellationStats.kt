package com.telemetrypro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.R
import com.telemetrypro.app.data.ConstellationStats
import com.telemetrypro.app.ui.theme.*

@Composable
fun ConstellationStatsCard(
    stats: List<ConstellationStats>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            stringResource(R.string.constellations_title),
            style = LabelCaps,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (stats.isEmpty()) {
            Text(stringResource(R.string.no_data), style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.4f))
        } else {
            stats.forEach { stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stat.constellation.flag + " " + stat.constellation.label,
                        style = CodeSm,
                        color = stat.constellation.color,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    val countColor = if (stat.usedInFix > 0) Secondary else OnSurfaceVariant
                    Text(
                        text = "${stat.usedInFix}/${stat.totalVisible}",
                        style = CodeSm,
                        color = countColor
                    )
                }
            }
        }
    }
}
