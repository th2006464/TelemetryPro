package com.telemetrypro.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrypro.app.LocaleHelper
import com.telemetrypro.app.R
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.data.LockStatus
import com.telemetrypro.app.data.SatelliteInfo
import com.telemetrypro.app.ui.components.TopAppBar
import com.telemetrypro.app.ui.theme.*

@Composable
fun SkyviewScreen(
    state: LocationState,
    isOnlineMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val svsLabel = stringResource(R.string.skyview_svs_label)
    val isZh = LocaleHelper.isZh(LocalContext.current)
    val sortedSats = remember(state.satellites) {
        state.satellites.sortedByDescending { it.snr }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top bar
        item(key = "topbar") {
            TopAppBar(
                fixLabel = "${state.usedSatellites}/${state.totalSatellites} $svsLabel",
                isFixed = state.usedSatellites > 0,
                isOnline = isOnlineMode
            )
        }

        item(key = "spacer1") { Spacer(Modifier.height(8.dp)) }

        // Constellation stats with country labels
        if (state.constellationStats.isNotEmpty()) {
            item(key = "constellation_stats") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.constellationStats.forEach { stat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(IntrinsicSize.Min)
                        ) {
                            Canvas(modifier = Modifier.size(10.dp)) {
                                drawCircle(color = stat.constellation.color)
                            }
                            Text(
                                stat.constellation.label,
                                style = CodeSm,
                                color = stat.constellation.color,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (isZh) stat.constellation.countryZh else stat.constellation.countryEn,
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 9.sp,
                                    lineHeight = 12.sp
                                ),
                                color = OnSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                "${stat.totalVisible}⬡",
                                style = TelemetryMd,
                                color = stat.constellation.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item(key = "spacer2") { Spacer(Modifier.height(12.dp)) }
        }

        // Satellite inventory header
        item(key = "inventory_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(TileBackground, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .border(1.dp, TileBorder, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .padding(12.dp)
            ) {
                Text(stringResource(R.string.skyview_inventory), style = LabelCaps, color = OnSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.skyview_header_sv), style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.width(44.dp))
                    Text(stringResource(R.string.skyview_header_sys), style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.width(60.dp))
                    Text(stringResource(R.string.skyview_header_el), style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.width(36.dp))
                    Text(stringResource(R.string.skyview_header_az), style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.width(40.dp))
                    Text(stringResource(R.string.skyview_header_snr), style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.weight(0.3f))
                    Text(stringResource(R.string.skyview_header_lock), style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.width(56.dp))
                }
            }
        }

        // Satellite rows — lazily rendered for smooth scrolling
        if (state.satellites.isEmpty()) {
            item(key = "empty") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(TileBackground, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                        .border(1.dp, TileBorder, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        stringResource(R.string.skyview_searching),
                        style = CodeSm,
                        color = OnSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            items(
                items = sortedSats,
                key = { "${it.constellation.label}${it.svid}" }
            ) { sat ->
                SatelliteRow(
                    sat = sat,
                    isLast = sat == sortedSats.last(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        item(key = "bottom_spacer") { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun SatelliteRow(
    sat: SatelliteInfo,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = if (isLast)
        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
    else
        RoundedCornerShape(0.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TileBackground, shape)
            .border(1.dp, TileBorder, shape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${sat.constellation.label.first()}${sat.svid.toString().padStart(2, '0')}",
            style = CodeSm,
            color = OnSurfaceVariant,
            modifier = Modifier.width(44.dp)
        )
        Text(
            sat.constellation.label,
            style = CodeSm,
            color = sat.constellation.color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp)
        )
        Text(
            "${sat.elevation.toInt()}°",
            style = CodeSm,
            color = OnSurfaceVariant,
            modifier = Modifier.width(36.dp)
        )
        Text(
            "${sat.azimuth.toInt()}°",
            style = CodeSm,
            color = OnSurfaceVariant,
            modifier = Modifier.width(40.dp)
        )

        Canvas(
            modifier = Modifier
                .weight(0.3f)
                .height(4.dp)
        ) {
            val ratio = (sat.snr / 50f).coerceIn(0f, 1f)
            drawRect(color = SurfaceContainerHighest)
            drawRect(
                color = sat.constellation.color,
                size = Size(size.width * ratio, size.height)
            )
        }

        val lockLabel = when (sat.lockStatus) {
            LockStatus.LOCKED -> stringResource(R.string.lock_locked)
            LockStatus.SYNCING -> stringResource(R.string.lock_syncing)
            LockStatus.SEARCHING -> stringResource(R.string.lock_searching)
        }
        Text(
            lockLabel,
            style = CodeSm,
            color = when (sat.lockStatus) {
                LockStatus.LOCKED -> Secondary
                LockStatus.SYNCING -> PrimaryFixedDim
                LockStatus.SEARCHING -> OnSurfaceVariant
            },
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(56.dp)
        )
    }
}
