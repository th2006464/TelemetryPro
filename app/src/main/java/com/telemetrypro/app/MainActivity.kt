package com.telemetrypro.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import com.telemetrypro.app.ui.screens.SettingsScreen
import com.telemetrypro.app.ui.screens.SkyviewScreen
import com.telemetrypro.app.ui.screens.TrendsScreen
import com.telemetrypro.app.ui.theme.TelemetryProTheme
import com.telemetrypro.app.viewmodel.GpsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            Toast.makeText(context, "GPS monitoring started", Toast.LENGTH_SHORT).show()
        }
    }

    // Request permission on launch
    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            launcher.launch(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                } else {
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }
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
                0 -> DashboardScreen(state = state)
                1 -> SkyviewScreen(state = state)
                2 -> TrendsScreen(state = state)
                3 -> SettingsScreen()
            }
        }

        // Bottom navigation
        BottomNavBar(
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}
