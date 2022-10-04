package com.example.finito.features.boards.presentation.screen.archive.components

import androidx.compose.runtime.Composable
import com.example.finito.core.presentation.components.dialogs.ErrorDialog
import com.example.finito.features.boards.presentation.screen.archive.ArchiveEvent
import com.example.finito.features.boards.presentation.screen.archive.ArchiveViewModel

@Composable
fun ArchiveDialogs(archiveViewModel: ArchiveViewModel) {
    when (archiveViewModel.dialogType) {
        is ArchiveEvent.DialogType.Error -> {
            val message = (archiveViewModel.dialogType as ArchiveEvent.DialogType.Error).message
            ErrorDialog(
                message = message,
                onDismiss = {
                    archiveViewModel.onEvent(ArchiveEvent.ShowDialog())
                },
                onConfirmButtonClick = {
                    archiveViewModel.onEvent(ArchiveEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}