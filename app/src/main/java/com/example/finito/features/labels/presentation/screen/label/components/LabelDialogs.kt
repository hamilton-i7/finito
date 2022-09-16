package com.example.finito.features.labels.presentation.screen.label.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.finito.R
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.dialogs.DeleteDialog
import com.example.finito.features.labels.presentation.components.EditLabelDialog
import com.example.finito.features.labels.presentation.screen.label.LabelEvent
import com.example.finito.features.labels.presentation.screen.label.LabelViewModel

@Composable
fun LabelDialogs(
    labelViewModel: LabelViewModel,
    onNavigateToHome: () -> Unit,
) {
    when (labelViewModel.dialogType) {
        LabelEvent.DialogType.Delete -> {
            DeleteDialog(
                onDismiss = {
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                },
                title = R.string.delete_label_question,
                description = R.string.delete_label_confirmation,
                onDismissClick = {
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                },
                onConfirmClick = {
                    labelViewModel.onEvent(LabelEvent.DeleteLabel)
                    onNavigateToHome()
                }
            )
        }
        LabelEvent.DialogType.Rename -> {
            EditLabelDialog(
                nameState = labelViewModel.labelNameState.copy(
                    onValueChange = {
                        labelViewModel.onEvent(LabelEvent.ChangeName(it))
                    }
                ),
                onDismiss = {
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                },
                onDismissClick = {
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                },
                onConfirmClick = {
                    labelViewModel.onEvent(LabelEvent.EditLabel)
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}