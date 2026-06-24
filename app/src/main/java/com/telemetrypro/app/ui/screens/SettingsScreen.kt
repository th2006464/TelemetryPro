package com.telemetrypro.app.ui.screens

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.ui.components.TopAppBar
import com.telemetrypro.app.ui.theme.*

/**
 * Settings Screen — units, coordinate format, data management, privacy.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            fixLabel = "OFFLINE MODE",
            isFixed = false
        )

        Spacer(Modifier.height(8.dp))

        // Header
        Text(
            "System Configuration",
            style = HeadlineLgMobile,
            color = OnBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Text(
            "CONTROL PANEL v4.2.0 // OFFLINE MODE ACTIVE",
            style = CodeSm,
            color = OnSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        Spacer(Modifier.height(8.dp))

        // ---- Distance Unit ----
        SettingsTile(
            title = "DISTANCE UNIT",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            var selected by remember { mutableStateOf(0) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("METRIC (km)", "IMPERIAL (mi)").forEachIndexed { i, label ->
                    val isSelected = i == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) PrimaryContainer.copy(alpha = 0.2f) else SurfaceContainerHigh,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) PrimaryFixedDim else OutlineVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selected = i }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = TelemetryMd,
                            color = if (isSelected) PrimaryFixedDim else OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---- Speed Unit ----
        SettingsTile(
            title = "VELOCITY VECTOR",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            var selected by remember { mutableStateOf(0) }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("KM/H", "KNOTS", "MPH").forEachIndexed { i, label ->
                    val isSelected = i == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) PrimaryContainer.copy(alpha = 0.2f) else SurfaceContainerHigh,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) PrimaryFixedDim else OutlineVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selected = i }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = LabelCaps,
                            color = if (isSelected) PrimaryFixedDim else OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---- Coordinate System ----
        SettingsTile(
            title = "COORDINATE REFERENCE SYSTEM",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val systems = listOf(
                    Triple("Decimal Degrees (DD)", "40.7128°N, 74.0060°W", true),
                    Triple("Deg Min Sec (DMS)", "40°42'46\"N", false),
                    Triple("UTM / UPS", "18T 583944 4507345", false)
                )
                systems.forEach { (name, example, enabled) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (enabled) SurfaceContainerLowest else SurfaceContainerHigh,
                                RoundedCornerShape(8.dp)
                            )
                            .then(
                                if (enabled) Modifier.border(2.dp, PrimaryFixedDim, RoundedCornerShape(8.dp))
                                else Modifier.alpha(0.5f)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(name, style = TelemetryMd, color = if (enabled) PrimaryFixedDim else OnSurfaceVariant)
                            Text(example, style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }

        // ---- Altitude Unit ----
        SettingsTile(
            title = "ALTITUDE REFERENCE",
            subtitle = "Mean Sea Level (MSL)",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            var selected by remember { mutableStateOf(0) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("m", "ft").forEachIndexed { i, label ->
                    val isSelected = i == selected
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) PrimaryContainer else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) PrimaryFixedDim else OutlineVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selected = i }
                            .size(48.dp, 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = TelemetryMd,
                            color = if (isSelected) OnPrimary else OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---- Data Management ----
        SettingsTile(
            title = "LOCAL STORAGE",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerLowest, RoundedCornerShape(8.dp))
                        .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                        .clickable { }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Export NMEA Logs", style = TelemetryMd, color = OnSurface)
                            Text("24.8 MB available for sync", style = CodeSm, color = OnSurfaceVariant)
                        }
                        Text("DL", style = LabelCaps, color = OnSurfaceVariant)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerLowest, RoundedCornerShape(8.dp))
                        .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                        .clickable { }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Clear Local Cache", style = TelemetryMd, color = Error)
                            Text("Delete all offline maps", style = CodeSm, color = OnSurfaceVariant)
                        }
                        Text("DEL", style = LabelCaps, color = Error)
                    }
                }
            }
        }

        // ---- Theme Locked ----
        SettingsTile(
            title = "INTERFACE PROFILE",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerHighest, RoundedCornerShape(8.dp))
                    .border(1.dp, PrimaryFixedDim, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("\uD83C\uDF19", style = TelemetryMd)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Tactical Dark", style = TelemetryMd, color = PrimaryFixedDim)
                        Text("Luminance restricted to 15cd/m2", style = CodeSm, color = PrimaryFixedDim.copy(alpha = 0.7f))
                    }
                }
            }
            Text(
                "Standard themes disabled in field mode.",
                style = CodeSm,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // ---- Privacy ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(PrimaryContainer.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .border(1.dp, PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("Purely Local & Offline", style = TelemetryMd, color = PrimaryFixedDim)
                Spacer(Modifier.height(4.dp))
                Text(
                    "TELEMETRY PRO transmits no personal data. All NMEA sentences, coordinate logs, and mapping caches are stored exclusively on your device.",
                    style = BodyMd,
                    color = OnSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Privacy Protocol", style = CodeSm, color = PrimaryFixedDim)
                    Text("System Manifest", style = CodeSm, color = PrimaryFixedDim)
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ============================================================
// Settings Tile wrapper
// ============================================================

@Composable
private fun SettingsTile(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                title.uppercase(),
                style = LabelCaps,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = TelemetryMd,
                    color = OnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            content()
        }
    }
}
