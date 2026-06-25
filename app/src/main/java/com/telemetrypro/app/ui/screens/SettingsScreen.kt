package com.telemetrypro.app.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.LocaleHelper
import com.telemetrypro.app.R
import com.telemetrypro.app.ui.components.TopAppBar
import com.telemetrypro.app.ui.theme.*

@Composable
fun SettingsScreen(
    isOnlineMode: Boolean = false,
    isNetworkAvailable: Boolean = false,
    gpsFixStatus: String = "3D FIX",
    isFixed: Boolean = false,
    nmeaLoggingEnabled: Boolean = false,
    onOnlineModeChanged: (Boolean) -> Unit = {},
    onLanguageChanged: () -> Unit = {},
    onNmeaLoggingChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isZh = LocaleHelper.isZh(context)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            fixLabel = gpsFixStatus,
            isFixed = isFixed,
            isOnline = isOnlineMode
        )

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(R.string.settings_title),
            style = HeadlineLgMobile,
            color = OnBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Text(
            stringResource(R.string.settings_subtitle),
            style = CodeSm,
            color = OnSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        Spacer(Modifier.height(8.dp))

        // ---- Network Mode ----
        SettingsTile(
            title = stringResource(R.string.network_mode),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Online mode button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isOnlineMode) PrimaryContainer.copy(alpha = 0.2f) else SurfaceContainerHigh,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isOnlineMode) PrimaryFixedDim else OutlineVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (!isOnlineMode) {
                                onOnlineModeChanged(true)
                            }
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.network_mode_online),
                            style = TelemetryMd,
                            color = if (isOnlineMode) PrimaryFixedDim else OnSurfaceVariant
                        )
                        Text(
                            stringResource(R.string.network_mode_online_desc),
                            style = CodeSm,
                            color = OnSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Offline mode button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!isOnlineMode) SurfaceContainerLowest else SurfaceContainerHigh,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (!isOnlineMode) Secondary else OutlineVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (isOnlineMode) {
                                onOnlineModeChanged(false)
                            }
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.network_mode_offline),
                            style = TelemetryMd,
                            color = if (!isOnlineMode) Secondary else OnSurfaceVariant
                        )
                        Text(
                            stringResource(R.string.network_mode_offline_desc),
                            style = CodeSm,
                            color = OnSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Network status indicator
            if (isOnlineMode) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statusColor = if (isNetworkAvailable) Secondary else WarningAmber
                    val statusText = if (isNetworkAvailable)
                        (if (isZh) "已连接网络" else "Network connected")
                    else
                        (if (isZh) "等待网络连接…" else "Waiting for network…")
                    Text(
                        text = if (isNetworkAvailable) "\uD83D\uDFE2" else "\u26A0\uFE0F",
                        style = CodeSm
                    )
                    Text(
                        statusText,
                        style = CodeSm,
                        color = statusColor
                    )
                }
            }
        }

        // ---- Language ----
        SettingsTile(
            title = stringResource(R.string.settings_language),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val langOptions = listOf(
                    "zh" to stringResource(R.string.settings_lang_chinese),
                    "en" to stringResource(R.string.settings_lang_english)
                )
                langOptions.forEach { (code, label) ->
                    val isSelected = (code == "zh" && isZh) || (code == "en" && !isZh)
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
                            .clickable {
                                if (code != if (isZh) "zh" else "en") {
                                    LocaleHelper.setLanguage(context, code)
                                    Toast.makeText(context, context.getString(R.string.lang_changed), Toast.LENGTH_SHORT).show()
                                    onLanguageChanged()
                                }
                            }
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

        // ---- Distance Unit & Altitude Reference (side by side) ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Distance Unit (left)
            SettingsTile(
                title = stringResource(R.string.settings_distance_unit),
                modifier = Modifier.weight(1f)
            ) {
                var selected by remember { mutableStateOf(0) }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("KM", "MI").forEachIndexed { i, label ->
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
                                .padding(vertical = 10.dp),
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

            // Altitude Reference (right)
            SettingsTile(
                title = stringResource(R.string.settings_altitude_ref),
                subtitle = stringResource(R.string.settings_altitude_msl),
                modifier = Modifier.weight(1f)
            ) {
                var selected by remember { mutableStateOf(0) }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("M", "FT").forEachIndexed { i, label ->
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
                                .padding(vertical = 10.dp),
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
        }

        // ---- Coordinate System ----
        SettingsTile(
            title = stringResource(R.string.settings_coord_system),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val systems = listOf(
                    Triple(stringResource(R.string.settings_crs_dd), stringResource(R.string.settings_crs_dd_example), true),
                    Triple(stringResource(R.string.settings_crs_dms), stringResource(R.string.settings_crs_dms_example), false),
                    Triple(stringResource(R.string.settings_crs_utm), stringResource(R.string.settings_crs_utm_example), false)
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

        // ---- Speed Unit ----
        SettingsTile(
            title = stringResource(R.string.settings_velocity_unit),
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

        // ---- Data Management ----
        SettingsTile(
            title = stringResource(R.string.settings_local_storage),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // NMEA recording toggle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerLowest, RoundedCornerShape(8.dp))
                        .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                        .clickable { onNmeaLoggingChanged(!nmeaLoggingEnabled) }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_nmea_record),
                                style = TelemetryMd,
                                color = OnSurface
                            )
                            Text(
                                stringResource(R.string.settings_nmea_record_subtitle),
                                style = CodeSm,
                                color = OnSurfaceVariant
                            )
                        }
                        Text(
                            if (nmeaLoggingEnabled)
                                stringResource(R.string.settings_nmea_on)
                            else
                                stringResource(R.string.settings_nmea_off),
                            style = LabelCaps,
                            color = if (nmeaLoggingEnabled) Secondary else OnSurfaceVariant.copy(alpha = 0.5f)
                        )
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
                            Text(stringResource(R.string.settings_export_nmea), style = TelemetryMd, color = OnSurface)
                            Text(stringResource(R.string.settings_export_subtitle), style = CodeSm, color = OnSurfaceVariant)
                        }
                        Text(stringResource(R.string.settings_export_btn), style = LabelCaps, color = OnSurfaceVariant)
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
                            Text(stringResource(R.string.settings_clear_cache), style = TelemetryMd, color = Error)
                            Text(stringResource(R.string.settings_clear_cache_subtitle), style = CodeSm, color = OnSurfaceVariant)
                        }
                        Text(stringResource(R.string.settings_delete_btn), style = LabelCaps, color = Error)
                    }
                }
            }
        }

        // ---- Theme Locked ----
        SettingsTile(
            title = stringResource(R.string.settings_interface_profile),
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
                        Text(stringResource(R.string.settings_tactical_dark), style = TelemetryMd, color = PrimaryFixedDim)
                        Text(stringResource(R.string.settings_luminance_desc), style = CodeSm, color = PrimaryFixedDim.copy(alpha = 0.7f))
                    }
                }
            }
            Text(
                stringResource(R.string.settings_theme_disabled),
                style = CodeSm,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // ---- Project & Contact ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(PrimaryContainer.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .border(1.dp, PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(stringResource(R.string.settings_privacy_title), style = TelemetryMd, color = PrimaryFixedDim)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.settings_privacy_desc),
                    style = BodyMd,
                    color = OnSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                // Project URL
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.settings_project_url) + ": ",
                        style = CodeSm,
                        color = PrimaryFixedDim
                    )
                    Text(
                        stringResource(R.string.settings_project_url_value),
                        style = CodeSm,
                        color = OnSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                // Contact
                Text(
                    stringResource(R.string.settings_contact_title) + ": " + stringResource(R.string.settings_contact_wechat),
                    style = CodeSm,
                    color = OnSurfaceVariant
                )
            }
        }

        // Version info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            val pkgInfo = remember {
                try {
                    context.packageManager.getPackageInfo(context.packageName, 0)
                } catch (e: Exception) { null }
            }
            val versionName = remember { pkgInfo?.versionName ?: "1.6.0" }
            val buildDate = remember {
                try {
                    val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date(pkgInfo?.lastUpdateTime ?: System.currentTimeMillis()))
                    date
                } catch (e: Exception) { "2026-06-24" }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Telemetry Pro v${versionName}",
                    style = CodeSm,
                    color = OnSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "Build: ${buildDate}",
                    style = CodeSm,
                    color = OnSurfaceVariant.copy(alpha = 0.35f)
                )
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

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
