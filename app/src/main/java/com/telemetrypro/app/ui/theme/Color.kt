package com.telemetrypro.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Astra Precision Color Palette — Full Design System
// Based on dark aviation glass cockpit aesthetic
// ============================================================

// --- Primary (Safety Yellow) ---
val Primary = Color(0xFFE9C400)
val PrimaryFixedDim = Color(0xFFE9C400)
val PrimaryFixed = Color(0xFFFFE16D)
val PrimaryContainer = Color(0xFFFFD700)
val OnPrimary = Color(0xFF3A3000)
val OnPrimaryContainer = Color(0xFF705E00)
val OnPrimaryFixed = Color(0xFF221B00)
val OnPrimaryFixedVariant = Color(0xFF544600)
val InversePrimary = Color(0xFF705D00)

// --- Secondary (Signal Green) ---
val Secondary = Color(0xFF78DC77)
val SecondaryFixedDim = Color(0xFF78DC77)
val SecondaryFixed = Color(0xFF94F990)
val SecondaryContainer = Color(0xFF00761F)
val OnSecondary = Color(0xFF00390A)
val OnSecondaryContainer = Color(0xFF95FB92)
val OnSecondaryFixed = Color(0xFF002204)
val OnSecondaryFixedVariant = Color(0xFF005313)

// --- Tertiary (Ice Blue) ---
val Tertiary = Color(0xFFF3F6FF)
val TertiaryFixedDim = Color(0xFF9ECAFF)
val TertiaryFixed = Color(0xFFD1E4FF)
val TertiaryContainer = Color(0xFFC2DCFF)
val OnTertiary = Color(0xFF003258)
val OnTertiaryContainer = Color(0xFF0062A4)
val OnTertiaryFixed = Color(0xFF001D36)
val OnTertiaryFixedVariant = Color(0xFF00497D)

// --- Error (Soft Red) ---
val Error = Color(0xFFFFB4AB)
val ErrorContainer = Color(0xFF93000A)
val OnError = Color(0xFF690005)
val OnErrorContainer = Color(0xFFFFDAD6)

// --- Surface Hierarchy (Dark) ---
val Background = Color(0xFF131313)
val Surface = Color(0xFF131313)
val SurfaceDim = Color(0xFF131313)
val SurfaceBright = Color(0xFF393939)
val SurfaceContainerLowest = Color(0xFF0E0E0E)
val SurfaceContainerLow = Color(0xFF1C1B1B)
val SurfaceContainer = Color(0xFF201F1F)
val SurfaceContainerHigh = Color(0xFF2A2A2A)
val SurfaceContainerHighest = Color(0xFF353534)
val SurfaceVariant = Color(0xFF353534)
val SurfaceTint = Color(0xFFE9C400)

// --- On-Surface Colors ---
val OnBackground = Color(0xFFE5E2E1)
val OnSurface = Color(0xFFE5E2E1)
val OnSurfaceVariant = Color(0xFFD0C6AB)
val InverseOnSurface = Color(0xFF313030)
val InverseSurface = Color(0xFFE5E2E1)

// --- Outline ---
val Outline = Color(0xFF999077)
val OutlineVariant = Color(0xFF4D4732)

// ============================================================
// Constellation Colors — 8 Satellite Systems
// ============================================================
object ConstellationColors {
    val GPS = Color(0xFF78DC77)          // Signal Green
    val GLONASS = Color(0xFF4FC3F7)      // Ice Blue
    val GALILEO = Color(0xFFCE93D8)      // Lavender Purple
    val BEIDOU = Color(0xFFFFB74D)       // Amber Orange
    val QZSS = Color(0xFF4DB6AC)         // Teal
    val IRNSS = Color(0xFFF48FB1)        // Pink
    val SBAS = Color(0xFF9E9E9E)         // Gray
    val UNKNOWN = Color(0xFF616161)       // Dark Gray
}

// ============================================================
// UI-Only Tones (not part of Material roles, used in components)
// ============================================================
val WarningAmber = Color(0xFFFFCA28)
val MutedGray = Color(0xFF757575)
val DeepDark = Color(0xFF0A0A0A)
val TileBackground = Color(0xFF1E1E1E)
val TileBorder = Color(0xFF333333)
