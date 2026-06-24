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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableIntStateOf(0) }
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
                    isOnlineMode = isOnlineMode
                )
                1 -> SkyviewScreen(
                    state = state,
                    isOnlineMode = isOnlineMode
                )
                2 -> TrendsScreen(
                    state = state,
                    isOnlineMode = isOnlineMode
                )
                3 -> RecordScreen(
                    state = state,
                    isRecording = isRecording,
                    distanceKm = recordingDistanceKm,
                    sessions = sessions,
                    onStartRecording = { name -> viewModel.startRecording(name) },
                    onStopRecording = { viewModel.stopRecording() },
                    onDeleteSession = { id -> viewModel.deleteSession(id) },
                    onRenameSession = { id, name -> viewModel.renameSession(id, name) }
                )
                4 -> SettingsScreen(
                    isOnlineMode = isOnlineMode,
                    isNetworkAvailable = isNetworkAvailable,
                    gpsFixStatus = when (state.fixStatus) {
                        com.telemetrypro.app.data.GpsFixStatus.FIXED -> "3D FIX"
                        com.telemetrypro.app.data.GpsFixStatus.SEARCHING -> context.getString(R.string.fix_status_searching)
                        com.telemetrypro.app.data.GpsFixStatus.NO_SIGNAL -> context.getString(R.string.fix_status_no_signal)
                    },
                    isFixed = state.fixStatus == com.telemetrypro.app.data.GpsFixStatus.FIXED,
                    onOnlineModeChanged = { enabled -> viewModel.setOnlineMode(enabled) },
                    onLanguageChanged = {
                        // Restart activity to apply new locale
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        Runtime.getRuntime().exit(0)
                    }
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
