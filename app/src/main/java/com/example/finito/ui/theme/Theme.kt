package com.example.finito.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = Vodka,
    onPrimary = StPatrickBlue,
    primaryContainer = BluePigment,
    onPrimaryContainer = PaleLavender,
    secondary = Mauve,
    onSecondary = PixiePowder,
    secondaryContainer = BlueMagentaViolet,
    onSecondaryContainer = Lavender,
    tertiary = Maize,
    onTertiary = AmericanBronze,
    tertiaryContainer = DarkBronze,
    onTertiaryContainer = Shandy,
    error = Melon,
    errorContainer = Sangria,
    onError = BloodRed,
    onErrorContainer = PalePink,
    background = EerieBlack,
    onBackground = PlatinumVariant,
    surface = EerieBlack,
    onSurface = PlatinumVariant,
    surfaceVariant = OuterSpace,
    onSurfaceVariant = LavenderGray,
    outline = SpanishGray,
    inverseOnSurface = EerieBlack,
    inverseSurface = PlatinumVariant,
)

private val LightColorScheme = lightColorScheme(
    primary = Liberty,
    onPrimary = White,
    primaryContainer = PaleLavender,
    onPrimaryContainer = Blue,
    secondary = RoyalPurple,
    onSecondary = White,
    secondaryContainer = Lavender,
    onSecondaryContainer = DeepViolet,
    tertiary = BronzeYellow,
    onTertiary = White,
    tertiaryContainer = Shandy,
    onTertiaryContainer = Brown,
    error = Carnelian,
    errorContainer = PalePink,
    onError = White,
    onErrorContainer = DarkChocolate,
    background = Snow,
    onBackground = EerieBlack,
    surface = Snow,
    onSurface = EerieBlack,
    surfaceVariant = Platinum,
    onSurfaceVariant = OuterSpace,
    outline = SonicSilver,
    inverseOnSurface = AntiFlashWhite,
    inverseSurface = DarkCharcoal
)

@Composable
fun FinitoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}