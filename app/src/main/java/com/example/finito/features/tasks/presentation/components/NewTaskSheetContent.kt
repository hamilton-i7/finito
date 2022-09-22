package com.example.finito.features.tasks.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.components.textfields.BasicTextField
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.boards.presentation.components.SelectedBoardIndicator

@Composable
fun NewTaskSheetContent(
    nameTextFieldState: TextFieldState,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onViewMoreOptionsClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    saveButtonEnabled: Boolean = true,
    includeBoardIndicator: Boolean = false,
    selectedBoardName: String = "",
    boardsMenuExpanded: Boolean = false,
    onBoardIndicatorClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        if (includeBoardIndicator) {
            SelectedBoardIndicator(
                boardName = selectedBoardName,
                expanded = boardsMenuExpanded,
                onIndicatorClick = onBoardIndicatorClick,
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
            )
        }

        BasicTextField(
            state = nameTextFieldState,
            placeholder = R.string.new_task,
            modifier = Modifier.focusRequester(focusRequester)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onViewMoreOptionsClick) {
                Text(text = stringResource(id = R.string.view_more_options))
            }

            TextButton(
                onClick = onSaveClick,
                enabled = saveButtonEnabled
            ) { Text(text = stringResource(id = R.string.save)) }
        }
    }
}