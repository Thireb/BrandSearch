package com.closetfinder.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

// System default font (Roboto) only: it ships with Android and falls back to
// Noto Sans Arabic/Urdu automatically for Urdu-script text the user types in.
// Body sizes are bumped one step above the Material defaults for fast,
// one-handed, low-attention reading.
val ClosetFinderTypography = Typography().let { base ->
    base.copy(
        bodyLarge = base.bodyLarge.copy(fontSize = 17.sp, lineHeight = 24.sp),
        bodyMedium = base.bodyMedium.copy(fontSize = 15.sp, lineHeight = 21.sp),
        titleLarge = base.titleLarge.copy(fontSize = 23.sp),
        labelLarge = base.labelLarge.copy(fontSize = 15.sp),
    )
}
