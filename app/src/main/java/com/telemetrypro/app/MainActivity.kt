package com.telemetrypro.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.telemetrypro.app.ui.components.BottomNavBar
import com.telemetrypro.app.ui.screens.DashboardScreen
import com.telemetrypro.app.ui.screens.FullscreenMapScreen
import com.telemetrypro.app.ui.screens.RecordScreen
import com.telemetrypro.app.ui.screens.SettingsScreen
import com.telemetrypro.app.ui.screens.SkyviewScreen
import com.telemetrypro.app.ui.screens.TrendsScreen
import com.telemetrypro.app.ui.theme.TelemetryProTheme
import com.telemetrypro.app.viewmodel.GpsViewModel

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        try {
            val wrapped = if (newBase != null) {
                LocaleHelper.wrapContext(newBase)
            } else newBase
            super.attachBaseContext(wrapped)
        } catch (e: Exception) {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.init(this)

        setContent {
            TelemetryProTheme {
                MainApp()
            }
        }
    }
}

@Composable
private fun MainApp() {
    val viewModel: GpsViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isOnlineMode by viewModel.isOnlineMode.collectAsStateWithLifecycle()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingDistanceKm by viewModel.recordingDistanceKm.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val nmeaLoggingEnabled by viewModel.nmeaLoggingEnabled.collectAsStateWithLifecycle()
    val cellInfo by viewModel.cellInfoProvider.cellInfo.collectAsStateWithLifecycle()
    val coordFormatDms by viewModel.coordFormatDms.collectAsStateWithLifecycle()
    val azimuth by viewModel.azimuth.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showFullscreenMap by remember { mutableStateOf(false) }
    val hasPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    var permissionGranted by remember { mutableStateOf(hasPermission) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        permissionGranted = granted
        if (granted) {
            viewModel.startMonitoring()
            Toast.makeText(context, context.getString(R.string.gps_started), Toast.LENGTH_SHORT).show()
        }
    }

    // Request permission on launch
    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.startMonitoring()
        }
    }

    // Lifecycle-aware start/stop
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (permissionGranted) viewModel.startMonitoring()
                }
                Lifecycle.Event.ON_PAUSE -> viewModel.stopMonitoring()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Screen content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    state = state,
                    isOnlineMode = isOnlineMode,
                    coordFormatDms = coordFormatDms,
                    onFullscreenClick = { showFullscreenMap = true }
                )
                1 -> SkyviewScreen(
                    state = state,
                    isOnlineMode = isOnlineMode
                )
                2 -> TrendsScreen(
                    state = state,
                    isOnlineMode = isOnlineMode,
                    cellInfo = cellInfo,
                    onRefreshCellInfo = { viewModel.refreshCellInfo() }
                )
                3 -> RecordScreen(
                    state = state,
                    isRecording = isRecording,
                    distanceKm = recordingDistanceKm,
                    sessions = sessions,
                    azimuth = azimuth,
                    onStartRecording = { name -> viewModel.startRecording(name) },
                    onStopRecording = { viewModel.stopRecording() },
                    onDeleteSession = { id -> viewModel.deleteSession(id) },
                    onRenameSession = { id, name -> viewModel.renameSession(id, name) }
                )
                4 -> SettingsScreen(
                    isOnlineMode = isOnlineMode,
                    isNetworkAvailable = isNetworkAvailable,
                    coordFormatDms = coordFormatDms,
                    gpsFixStatus = when (state.fixStatus) {
                        com.telemetrypro.app.data.GpsFixStatus.FIXED -> "3D FIX"
                        com.telemetrypro.app.data.GpsFixStatus.SEARCHING -> context.getString(R.string.fix_status_searching)
                        com.telemetrypro.app.data.GpsFixStatus.NO_SIGNAL -> context.getString(R.string.fix_status_no_signal)
                    },
                    isFixed = state.fixStatus == com.telemetrypro.app.data.GpsFixStatus.FIXED,
                    nmeaLoggingEnabled = nmeaLoggingEnabled,
                    onOnlineModeChanged = { enabled -> viewModel.setOnlineMode(enabled) },
                    onLanguageChanged = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        // Activity will be recreated with new locale via attachBaseContext
                    },
                    onNmeaLoggingChanged = { enabled -> viewModel.setNmeaLoggingEnabled(enabled) },
                    onCoordFormatChanged = { dms -> viewModel.setCoordFormat(dms) }
                )
            }

            // Fullscreen map overlay
            if (showFullscreenMap) {
                FullscreenMapScreen(
                    state = state,
                    isOnlineMode = isOnlineMode,
                    onClose = { showFullscreenMap = false }
                )
            }
        }

        // Bottom navigation
        BottomNavBar(
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}
