package com.example.tripplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = SeaGrey,
    onSecondary = Color.White,
    secondaryContainer = SeaGreyContainer,
    onSecondaryContainer = OnSeaGreyContainer,
    tertiary = Coral,
    onTertiary = Color.White,
    tertiaryContainer = CoralContainer,
    onTertiaryContainer = OnCoralContainer,
    background = SandBackground,
    onBackground = InkText,
    surface = SandBackground,
    onSurface = InkText,
    surfaceVariant = MistVariant,
    onSurfaceVariant = OnMistVariant,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F6F5),
    surfaceContainer = Color(0xFFEAF1F0),
    surfaceContainerHigh = Color(0xFFE4EBEA),
    surfaceContainerHighest = Color.White,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
)

private val DarkColors = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = OnTealDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = OnTealContainerDark,
    secondary = SeaGreyDark,
    onSecondary = OnSeaGreyDark,
    secondaryContainer = SeaGreyContainerDark,
    onSecondaryContainer = OnSeaGreyContainerDark,
    tertiary = CoralDark,
    onTertiary = OnCoralDark,
    tertiaryContainer = CoralContainerDark,
    onTertiaryContainer = OnCoralContainerDark,
    background = NightBackground,
    onBackground = NightText,
    surface = NightBackground,
    onSurface = NightText,
    surfaceVariant = OnMistVariant,
    onSurfaceVariant = OutlineVariantLight,
    surfaceContainerLowest = Color(0xFF0A0F0F),
    surfaceContainerLow = Color(0xFF161D1D),
    surfaceContainer = Color(0xFF1A2121),
    surfaceContainerHigh = Color(0xFF242B2B),
    surfaceContainerHighest = Color(0xFF2F3636),
    outline = Color(0xFF899392),
    outlineVariant = OnMistVariant,
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
)

@Composable
fun TripPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}