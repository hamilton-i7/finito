package com.example.finito.features.boards.presentation.screen.home.components

import androidx.compose.runtime.Composable
import com.example.finito.core.presentation.components.dialogs.ErrorDialog
import com.example.finito.features.boards.presentation.screen.home.HomeEvent
import com.example.finito.features.boards.presentation.screen.home.HomeViewModel

@Composable
fun HomeDialogs(homeViewModel: HomeViewModel) {
    when (homeViewModel.dialogType) {
        is HomeEvent.DialogType.Error -> {
            val message = (homeViewModel.dialogType as HomeEvent.DialogType.Error).message
            ErrorDialog(
                message = message,
                onDismiss = {
                    homeViewModel.onEvent(HomeEvent.ShowDialog())
                },
                onConfirmButtonClick = {
                    homeViewModel.onEvent(HomeEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}