package com.example.finito.core.presentation.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class SnackbarState(
    val snackbarHostState: SnackbarHostState,
    val scope: CoroutineScope,
) {

    fun showSnackbar(
        context: Context,
        @StringRes message: Int,
        @StringRes actionLabel: Int?,
        onActionClick: (() -> Unit)?,
    ) {
        // Dismiss current Snackbar to avoid having multiple instances
        snackbarHostState.currentSnackbarData?.dismiss()
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = context.resources.getString(message),
                actionLabel = actionLabel?.let { context.resources.getString(it) },
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onActionClick?.invoke()
            }
        }
    }
}

@Composable
fun rememberSnackbarState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scope: CoroutineScope = rememberCoroutineScope(),
) = remember(snackbarHostState, scope) {
    SnackbarState(snackbarHostState, scope)
}