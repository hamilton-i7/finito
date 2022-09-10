package com.example.finito.core.presentation.components.util

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import com.example.finito.core.presentation.components.util.AnimationDurationConstants.RegularDurationMillis
import com.example.finito.core.presentation.components.util.AnimationDurationConstants.ShortDurationMillis

@OptIn(ExperimentalAnimationApi::class)
object NavigationTransitions  {
        val mainScreenEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        fadeIn(
            animationSpec = tween(
                durationMillis = ShortDurationMillis,
                easing = LinearOutSlowInEasing,
                delayMillis = RegularDurationMillis
            ),
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = ShortDurationMillis,
                easing = LinearOutSlowInEasing,
                delayMillis = RegularDurationMillis
            ),
            initialScale = 0.6f
        )
    }
    val mainScreenExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        fadeOut(
            animationSpec = tween(
                durationMillis = RegularDurationMillis,
                easing = FastOutLinearInEasing
            )
        )
    }

    val dialogScreenEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        slideInVertically { it / 2 } + fadeIn()
    }

    val dialogScreenExistTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutVertically { it / 2 } + fadeOut()
    }
}