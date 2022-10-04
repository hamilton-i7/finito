package com.example.finito.core.presentation.util

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle

enum class Keyboard {
    OPENED, CLOSED
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun keyboardAsState(): State<Keyboard> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val isResumed = lifecycle.currentState == Lifecycle.State.RESUMED
    return rememberUpdatedState(
        newValue = if (WindowInsets.isImeVisible && isResumed) Keyboard.OPENED else Keyboard.CLOSED
    )
}