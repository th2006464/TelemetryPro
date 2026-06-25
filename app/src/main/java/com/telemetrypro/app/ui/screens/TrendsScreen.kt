package com.telemetrypro.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.R
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.data.NetworkCellInfoProvider
import com.telemetrypro.app.ui.components.TopAppBar
import com.telemetrypro.app.ui.theme.*
import kotlin.math.min as mathMin

@Composable
fun TrendsScreen(
    state: LocationState,
    isOnlineMode: Boolean = false,
    cellInfo: NetworkCellInfoProvider.CellTowerInfo = NetworkCellInfoProvider.CellTowerInfo(),
    onRefreshCellInfo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            fixLabel = stringResource(R.string.fix_status_tracking),
            isFixed = state.speedKmh > 0,
            isOnline = isOnlineMode
        )

        Spacer(Modifier.height(8.dp))

        // Integrated: Left speed circle + Right VSI & altitude
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: Speedometer (compact)
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .background(TileBackground, RoundedCornerShape(12.dp))
                    .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            ) {
                SpeedCircle(
                    speedKmh = state.speedKmh,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    text = stringResource(R.string.trends_velocity_profile),
                    style = LabelCaps,
                    color = OnSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }

            // Right: VSI gauge + Altitude info
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VsiCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                TerrainSummary(
                    altitude = state.altitudeMeters,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }

        // Location source info panel
        // Pass individual fields (not the whole state) so Compose can skip this
        // card's recomposition when high-frequency fields like state.satellites
        // change but this card's inputs don't.
        LocationSourceInfoCard(
            provider = state.provider,
            ttffMillis = state.ttffMillis,
            isOnlineMode = state.isOnlineMode,
            isNetworkAvailable = state.isNetworkAvailable,
            latitude = state.latitude,
            longitude = state.longitude,
            accuracy = state.accuracy,
            altitudeMeters = state.altitudeMeters,
            speedKmh = state.speedKmh,
            bearing = state.bearing,
            usedSatellites = state.usedSatellites,
            totalSatellites = state.totalSatellites,
            nmeaLogLineCount = state.nmeaLogLines.size,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Cellular tower info panel
        CellTowerInfoCard(
            cellInfo = cellInfo,
            onRefresh = onRefreshCellInfo,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun CellTowerInfoCard(
    cellInfo: NetworkCellInfoProvider.CellTowerInfo,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            // NOTE: Do NOT put .clickable on this Column — it crashes on cold start
            // when combined with verticalScroll in the parent. Only the refresh text
            // below is clickable. Also use if-else, NOT return@Column (same issue).
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "基站信息（网络定位用）",
                style = LabelCaps,
                color = OnSurfaceVariant
            )
            Text(
                "点击刷新",
                style = CodeSm,
                color = OnSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.clickable { onRefresh() }
            )
        }

        Spacer(Modifier.height(8.dp))

        if (!cellInfo.available) {
            Text(
                "无法读取基站信息（需要定位权限）",
                style = CodeSm,
                color = OnSurfaceVariant.copy(alpha = 0.4f)
            )
        } else {
            // Operator / network
            InfoRow("运营商", cellInfo.operatorName, valueColor = PrimaryFixedDim)
            InfoRow("网络类型", cellInfo.networkTypeName + if (cellInfo.isRoaming) " · 漫游" else "")
            InfoRow("信号强度", "${cellInfo.level}/4" + if (cellInfo.rsrpDbm != Int.MIN_VALUE) " · ${cellInfo.rsrpDbm} dBm" else "")

            Spacer(Modifier.height(8.dp))

            Text(
                "服务小区（Serving Cell）",
                style = LabelCaps,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            InfoRow("MCC 国家码", cellInfo.mcc)
            InfoRow("MNC 网络码", cellInfo.mnc)
            InfoRow("小区标识 CI", cellInfo.cellId)
            InfoRow("跟踪区码 TAC", cellInfo.tac)
            InfoRow("物理小区 PCI", cellInfo.pci)
            InfoRow("频点", cellInfo.band)
            if (cellInfo.rsrpDbm != Int.MIN_VALUE) InfoRow("参考信号功率 RSRP", "${cellInfo.rsrpDbm} dBm")
            if (cellInfo.rsrqDb != Int.MIN_VALUE) InfoRow("参考信号质量 RSRQ", "${cellInfo.rsrqDb} dB")
            InfoRow("邻区数量", "${cellInfo.neighborCount}")

            Spacer(Modifier.height(8.dp))

            Text(
                "说明：网络辅助定位正是通过这些基站参数（CI、TAC、PCI、信号强度）" +
                "向系统查询位置估算的。MCC 标识国家（中国=460），MNC 标识运营商" +
                "（如移动=00/02，联通=01，电信=11）。RSRP 越接近 0 信号越强，" +
                "一般 -80 dBm 以上为优，-110 dBm 以下为弱。点击卡片可手动刷新。",
                style = CodeSm,
                color = OnSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun LocationSourceInfoCard(
    provider: String,
    ttffMillis: Long,
    isOnlineMode: Boolean,
    isNetworkAvailable: Boolean,
    latitude: Double,
    longitude: Double,
    accuracy: Float,
    altitudeMeters: Double,
    speedKmh: Float,
    bearing: Float,
    usedSatellites: Int,
    totalSatellites: Int,
    nmeaLogLineCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            "定位来源与系统信息",
            style = LabelCaps,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // --- Current provider ---
        InfoRow(
            label = "当前定位来源",
            value = when {
                provider.isEmpty() -> "未定位"
                provider == "gps" -> "卫星定位（GPS / GNSS）"
                provider == "network" -> "网络辅助定位（基站 / Wi-Fi）"
                provider == "fused" -> "融合定位（卫星 + 网络）"
                else -> provider
            },
            valueColor = if (provider == "gps") Secondary else PrimaryFixedDim
        )

        // --- TTFF ---
        if (ttffMillis > 0) {
            InfoRow(
                label = "首次定位时间",
                value = "${"%.1f".format(ttffMillis / 1000.0)} 秒",
                hint = "从启动到获取第一个有效定位的时间"
            )
        }

        Spacer(Modifier.height(8.dp))

        // --- Network status ---
        Text(
            "网络辅助定位状态",
            style = LabelCaps,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        InfoRow(
            label = "辅助定位开关",
            value = if (isOnlineMode) "已开启" else "已关闭",
            valueColor = if (isOnlineMode) Secondary else OnSurfaceVariant
        )
        InfoRow(
            label = "网络连接",
            value = if (isNetworkAvailable) "已连接" else "未连接",
            valueColor = if (isNetworkAvailable) Secondary else OnSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        // --- Provider capabilities comparison ---
        Text(
            "各定位方式可提供的信息",
            style = LabelCaps,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Header row
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text("信息项", style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.weight(0.3f))
            Text("卫星定位", style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.weight(0.23f))
            Text("网络辅助", style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.weight(0.23f))
            Text("当前值", style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.weight(0.24f))
        }

        CapabilityRow("经纬度", "✓ 精确", "✓ 粗略", String.format("%.4f, %.4f", latitude, longitude))
        CapabilityRow("精度", "1-10米", "10-100米", "${"%.1f".format(accuracy)} 米")
        CapabilityRow("海拔", "✓ 可靠", "✗ 不可靠", if (altitudeMeters != 0.0) "${"%.0f".format(altitudeMeters)} 米" else "—")
        CapabilityRow("速度", "✓ 可靠", "✗ 通常为0", "${"%.1f".format(speedKmh)} km/h")
        CapabilityRow("方向", "✓ 可靠", "✗ 通常无", if (bearing > 0) "${bearing.toInt()}°" else "—")
        CapabilityRow("卫星数", "✓", "✗ 无", "$usedSatellites/$totalSatellites")
        CapabilityRow("NMEA数据", "✓", "✗ 无", if (nmeaLogLineCount > 0) "$nmeaLogLineCount 行" else "—")

        Spacer(Modifier.height(8.dp))

        // --- Explanation ---
        Text(
            "说明：卫星定位通过 GNSS 芯片接收卫星信号，精度高但室内信号弱；" +
            "网络辅助定位通过基站和 Wi-Fi 估算位置，精度低但室内可用，且能加速卫星首次定位。" +
            "本应用中卫星定位始终优先，网络辅助仅在卫星失效时接管。",
            style = CodeSm,
            color = OnSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = PrimaryFixedDim,
    hint: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.weight(0.35f))
        Text(value, style = CodeSm, color = valueColor, modifier = Modifier.weight(0.65f))
    }
    if (hint.isNotEmpty()) {
        Text(
            "  $hint",
            style = CodeSm,
            color = OnSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
        )
    }
}

@Composable
private fun CapabilityRow(
    item: String,
    gps: String,
    network: String,
    current: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(item, style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.8f), modifier = Modifier.weight(0.3f))
        Text(gps, style = CodeSm, color = Secondary.copy(alpha = 0.8f), modifier = Modifier.weight(0.23f))
        Text(network, style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.weight(0.23f))
        Text(current, style = CodeSm, color = PrimaryFixedDim, modifier = Modifier.weight(0.24f))
    }
}

@Composable
private fun SpeedCircle(
    speedKmh: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.1f", speedKmh),
                style = DisplayData,
                color = PrimaryFixedDim
            )
            Text(
                text = stringResource(R.string.dashboard_unit_kmh),
                style = LabelCaps,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun TerrainSummary(
    altitude: Double,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.trends_alt_label).replace("%d", altitude.toInt().toString()),
                style = TelemetryMd,
                color = OnSurfaceVariant
            )
            Text(
                String.format("%.4f, %.4f", latitude, longitude),
                style = CodeSm,
                color = OnSurfaceVariant.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun VsiCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.trends_vsi),
                style = LabelCaps,
                color = OnSurfaceVariant
            )
            Text(
                "+2.4",
                style = TelemetryMd,
                color = Secondary
            )
        }
    }
}

@Composable
private fun TerrainCard(
    altitude: Double,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
        ) {
            Text(stringResource(R.string.trends_terrain_context), style = LabelCaps, color = PrimaryFixedDim)
            Text(
                stringResource(R.string.trends_alt_label).replace("%d", altitude.toInt().toString()),
                style = TelemetryMd,
                color = OnSurfaceVariant
            )
            Text(
                String.format("%.4f, %.4f", latitude, longitude),
                style = CodeSm,
                color = OnSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
