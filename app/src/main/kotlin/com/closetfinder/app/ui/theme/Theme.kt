package com.closetfinder.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A fixed clay-seeded palette, not the wallpaper-based dynamic color API:
// the brand color must stay the same on every device.
private val LightColors = lightColorScheme(
    primary = ClayPrimary,
    onPrimary = Color.White,
    primaryContainer = ClayPrimaryContainerLight,
    onPrimaryContainer = ClayOnPrimaryContainerLight,
    secondary = StoneSecondary,
    secondaryContainer = StoneSecondaryContainerLight,
    tertiary = SaleGreen,
    tertiaryContainer = SaleGreenContainerLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    error = ErrorRed,
    errorContainer = ErrorContainerLight,
)

private val DarkColors = darkColorScheme(
    primary = ClayPrimaryDark,
    primaryContainer = ClayPrimaryContainerDark,
    onPrimaryContainer = ClayOnPrimaryContainerDark,
    secondary = StoneSecondaryDark,
    tertiary = SaleGreenDark,
    tertiaryContainer = SaleGreenContainerDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    errorContainer = ErrorContainerDark,
)

@Composable
fun ClosetFinderTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ClosetFinderTypography,
        content = content,
    )
}
