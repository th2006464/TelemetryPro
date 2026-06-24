package com.telemetrypro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.R
import com.telemetrypro.app.ui.theme.*

@Composable
fun NmeaFeed(
    lines: List<String>,
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
            Text(stringResource(R.string.nmea_raw_stream), style = LabelCaps, color = OnSurfaceVariant)
            Text(
                if (lines.isNotEmpty()) stringResource(R.string.nmea_logging) else stringResource(R.string.nmea_idle),
                style = CodeSm,
                color = if (lines.isNotEmpty()) PrimaryFixedDim else OnSurfaceVariant
            )
        }

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(SurfaceContainerLowest, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (lines.isEmpty()) {
                    Text(
                        stringResource(R.string.nmea_waiting),
                        style = CodeSm,
                        color = OnSurfaceVariant.copy(alpha = 0.3f)
                    )
                } else {
                    lines.takeLast(15).forEach { line ->
                        Text(
                            text = line,
                            style = CodeSm.copy(
                                color = OnSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
