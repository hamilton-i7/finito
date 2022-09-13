package com.example.finito.core.presentation.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.example.finito.R
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class SnackbarState(
    val snackbarHostState: SnackbarHostState,
    val scope: CoroutineScope,
    val navController: NavHostController,
) {

    fun showSnackbar(
        context: Context,
        @StringRes message: Int,
        onActionClick: () -> Unit,
    ) {
        // Dismiss current Snackbar to avoid having multiple instances
        snackbarHostState.currentSnackbarData?.dismiss()
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = context.resources.getString(message),
                actionLabel = context.resources.getString(R.string.undo),
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onActionClick()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun rememberSnackbarState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navController: NavHostController = rememberAnimatedNavController(),
    scope: CoroutineScope = rememberCoroutineScope(),
) = remember(snackbarHostState, navController, scope) {
    SnackbarState(snackbarHostState, scope, navController)
}