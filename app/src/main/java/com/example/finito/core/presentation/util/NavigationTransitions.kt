package com.example.finito.core.presentation.util

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.navigation.NavBackStackEntry
import com.example.finito.core.presentation.util.AnimationDurationConstants.LongDurationMillis
import com.example.finito.core.presentation.util.AnimationDurationConstants.RegularDurationMillis
import com.example.finito.core.presentation.util.AnimationDurationConstants.ShortestDurationMillis

@OptIn(ExperimentalAnimationApi::class)
object NavigationTransitions  {
        val peerScreenEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        fadeIn(
            animationSpec = tween(
                durationMillis = RegularDurationMillis,
                delayMillis = ShortestDurationMillis,
                easing = EaseOut
            ),
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = RegularDurationMillis,
                delayMillis = ShortestDurationMillis,
                easing = EaseOut
            ),
            initialScale = 0.92f
        )
    }
    val peerScreenExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        fadeOut(
            animationSpec = tween(
                durationMillis = ShortestDurationMillis,
                easing = FastOutLinearInEasing,
            )
        )
    }

    val childScreenEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        fadeIn(
            animationSpec = tween(
                durationMillis = RegularDurationMillis,
                delayMillis = ShortestDurationMillis,
                easing = FastOutLinearInEasing
            )
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = LongDurationMillis,
                easing = CubicBezierEasing(0.8f, 0f, 0.4f, 0f)
            ),
            initialScale = 0.8f
        )
    }

    val childScreenPopEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?) = {
        fadeIn(
            animationSpec = tween(
                durationMillis = ShortestDurationMillis,
                delayMillis = RegularDurationMillis,
                easing = LinearOutSlowInEasing
            )
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = LongDurationMillis,
                easing = CubicBezierEasing(0.8f, 0f, 0.4f, 0f)
            ),
            initialScale = 1.1f
        )
    }

    val childScreenExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        fadeOut(
            tween(
                durationMillis = RegularDurationMillis,
                easing = FastOutLinearInEasing
            )
        ) + scaleOut(
            tween(
                durationMillis = LongDurationMillis,
                easing = CubicBezierEasing(0.8f, 0f, 0.4f, 0f)
            ),
            targetScale = 1.1f
        )
    }

    val childScreenPopExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?) = {
        fadeOut(
            tween(
                durationMillis = RegularDurationMillis,
                easing = LinearOutSlowInEasing
            )
        ) + scaleOut(
            tween(
                durationMillis = LongDurationMillis,
                easing = CubicBezierEasing(0.8f, 0f, 0.4f, 0f)
            ),
            targetScale = 0.8f
        )
    }
}