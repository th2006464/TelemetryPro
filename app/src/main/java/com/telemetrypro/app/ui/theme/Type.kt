package com.telemetrypro.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.telemetrypro.app.R

// ============================================================
// Astra Precision Type System — 7-Level Hierarchy
// 2 Typefaces: Inter (labels/body) + JetBrains Mono (data/code)
// ============================================================

// Embedded font families
val InterFamily = FontFamily.Default // Fallback to system — Inter is available on Android
val JetBrainsMonoFamily = FontFamily.Monospace // Fallback to system monospace

// --- Named Type Classes ---

/** display-data: 48sp / 56sp line / -2% tracking / 700 weight — JetBrains Mono */
val DisplayData = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontSize = 48.sp,
    lineHeight = 56.sp,
    letterSpacing = (-0.02).sp,
    fontWeight = FontWeight.Bold
)

/** headline-lg-desktop: 32sp / 40sp line / 600 weight — Inter */
val HeadlineLgDesktop = TextStyle(
    fontFamily = InterFamily,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    fontWeight = FontWeight.SemiBold
)

/** headline-lg-mobile: 24sp / 32sp line / 600 weight — Inter */
val HeadlineLgMobile = TextStyle(
    fontFamily = InterFamily,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    fontWeight = FontWeight.SemiBold
)

/** telemetry-md: 18sp / 24sp line / 500 weight — JetBrains Mono */
val TelemetryMd = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Medium
)

/** body-md: 16sp / 24sp line / 400 weight — Inter */
val BodyMd = TextStyle(
    fontFamily = InterFamily,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Normal
)

/** label-caps: 12sp / 16sp line / 8% letter-spacing / 700 weight — Inter */
val LabelCaps = TextStyle(
    fontFamily = InterFamily,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.96.sp, // 0.08em = 0.96sp at 12sp
    fontWeight = FontWeight.Bold
)

/** code-sm: 12sp / 16sp line / 400 weight — JetBrains Mono */
val CodeSm = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Normal
)

// --- Material 3 Typography bridge ---
val AstraTypography = Typography(
    displayLarge = DisplayData,
    headlineLarge = HeadlineLgDesktop,
    headlineMedium = HeadlineLgMobile,
    titleMedium = TelemetryMd,
    bodyLarge = BodyMd,
    labelSmall = CodeSm,
    labelMedium = LabelCaps,
)
