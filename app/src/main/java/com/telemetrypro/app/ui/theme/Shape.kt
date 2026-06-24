package com.telemetrypro.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================
// Astra Precision Shape System
// 4px baseline grid:
//   DEFAULT = 2px  (0.125rem) → corners to 2dp
//   lg      = 4px  (0.25rem)  → corners to 4dp
//   xl      = 8px  (0.5rem)   → corners to 8dp
//   full    = 12px (0.75rem)  → corners to 12dp
// ============================================================

val AstraShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),   // DEFAULT
    small = RoundedCornerShape(4.dp),         // lg
    medium = RoundedCornerShape(8.dp),        // xl
    large = RoundedCornerShape(12.dp),        // full (instrument tiles)
    extraLarge = RoundedCornerShape(16.dp)    // full+ (map cards)
)
