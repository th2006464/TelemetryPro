package com.telemetrypro.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// ============================================================
// Astra Precision Dark Theme — Material 3 Override
// Maps all DESIGN.md colors to M3 color roles
// ============================================================

private val AstraDarkColorScheme = darkColorScheme(
    // Primary — Safety Yellow
    primary = PrimaryFixedDim,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    // Secondary — Signal Green
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    // Tertiary — Ice Blue
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    // Error
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    // Background & Surface
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,

    // Outline
    outline = Outline,
    outlineVariant = OutlineVariant,
)

@Composable
fun TelemetryProTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AstraDarkColorScheme,
        typography = AstraTypography,
        shapes = AstraShapes,
        content = content
    )
}
