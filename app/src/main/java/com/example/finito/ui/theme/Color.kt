package com.example.finito.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

const val DisabledAlpha = 0.38f

// Primary
val Liberty = Color(0xFF4A56AF)
val PaleLavender = Color(0xFFDDE0FF)
val Blue = Color(0xFF000866)
val Vodka = Color(0xFFBBC3FF)
val StPatrickBlue = Color(0xFF17247E)
val BluePigment = Color(0xFF313D95)

// Secondary
val RoyalPurple = Color(0xFF6F49AE)
val Lavender = Color(0xFFEDDCFF)
val DeepViolet = Color(0xFF260058)
val Mauve = Color(0xFFD6BAFF)
val PixiePowder = Color(0xFF3F127C)
val BlueMagentaViolet = Color(0xFF573094)

// Tertiary
val BronzeYellow = Color(0xFF6B5F00)
val Shandy = Color(0xFFF5E468)
val Brown = Color(0xFF201C00)
val Maize = Color(0xFFD8C84F)
val AmericanBronze = Color(0xFF383100)
val DarkBronze = Color(0xFF504700)

// Error
val Carnelian = Color(0xFFBA1B1B)
val PalePink = Color(0xFFFFDAD4)
val DarkChocolate = Color(0xFF410001)
val Melon = Color(0xFFFFB4A9)
val Sangria = Color(0xFF930006)
val BloodRed = Color(0xFF680003)

// Neutral
val Snow = Color(0xFFFFFBFD)
val EerieBlack = Color(0xFF1D1B1F)
val Platinum = Color(0xFFE3E1EC)
val PlatinumVariant = Color(0xFFE6E1E5)
val OuterSpace = Color(0xFF46464E)
val SonicSilver = Color(0xFF767680)
val AntiFlashWhite = Color(0xFFF5EFF4)
val DarkCharcoal = Color(0xFF323033)
val White = Color.White
val LavenderGray = Color(0xFFC7C5D0)
val SpanishGray = Color(0xFF91909A)

// Extras
val Water = Color(0xFFD1E4FF)
val MaastrichtBlue = Color(0xFF001C38)
val Dandelion = Color(0xFFE7EA3C)
val BlackChocolate = Color(0xFF1c1d00)
val UnbleachedSilk = Color(0xFFFFDAD3)
val DarkChocolateVariant = Color(0xFF410000)

val DarkCerulean = Color(0xFF004882)
val DarkBronzeCoin = Color(0xFF484A00)
val Kobe = Color(0xFF7D2B20)

data class FinitoColors(
    val colorScheme: ColorScheme,
    val lowPriorityContainer: Color,
    val onLowPriorityContainer: Color,
    val mediumPriorityContainer: Color,
    val onMediumPriorityContainer: Color,
    val urgentPriorityContainer: Color,
    val onUrgentPriorityContainer: Color,
) {
    val primary: Color get() = colorScheme.primary
    val onPrimary: Color get() = colorScheme.onPrimary
    val primaryContainer: Color get() = colorScheme.primaryContainer
    val onPrimaryContainer: Color get() = colorScheme.onPrimaryContainer
    val secondary: Color get() = colorScheme.secondary
    val onSecondary: Color get() = colorScheme.onSecondary
    val secondaryContainer: Color get() = colorScheme.secondaryContainer
    val onSecondaryContainer: Color get() = colorScheme.onSecondaryContainer
    val tertiary: Color get() = colorScheme.tertiary
    val onTertiary: Color get() = colorScheme.onTertiary
    val tertiaryContainer: Color get() = colorScheme.tertiaryContainer
    val onTertiaryContainer: Color get() = colorScheme.onTertiaryContainer
    val error: Color get() = colorScheme.error
    val errorContainer: Color get() = colorScheme.errorContainer
    val onError: Color get() = colorScheme.onError
    val onErrorContainer: Color get() = colorScheme.onErrorContainer
    val background: Color get() = colorScheme.background
    val onBackground: Color get() = colorScheme.onBackground
    val surface: Color get() = colorScheme.surface
    val onSurface: Color get() = colorScheme.onSurface
    val surfaceVariant: Color get() = colorScheme.surfaceVariant
    val onSurfaceVariant: Color get() = colorScheme.onSurfaceVariant
    val outline: Color get() = colorScheme.outline
    val inverseOnSurface: Color get() = colorScheme.inverseOnSurface
    val inverseSurface: Color get() = colorScheme.inverseSurface

    fun surfaceColorAtElevation(elevation: Dp) = colorScheme.surfaceColorAtElevation(elevation)
}